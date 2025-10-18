package tillerino.tillerinobot.diff;// Copyright (c) ppy Pty Ltd <contact@ppy.sh>. Licensed under the MIT Licence.

// See the LICENCE file in the repository root for full licence text.

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiScore;

public class OsuPerformanceCalculator
{
    public static final double PERFORMANCE_BASE_MULTIPLIER = 1.15; // This is being adjusted to keep the final pp value scaled around what it used to be when changing things.

    private boolean usingClassicSliderAccuracy;

    private double accuracy;
    private int scoreMaxCombo;
    private int countGreat;
    private int countOk;
    private int countMeh;
    private int countMiss;

    /**
     * Missed slider ticks that includes missed reverse arrows. Will only be correct on non-classic scores
     */
    private int countSliderTickMiss;

    /**
     * Amount of missed slider tails that don't break combo. Will only be correct on non-classic scores
     */
    private int countSliderEndsDropped;

    /**
     * Estimated total amount of combo breaks
     */
    private double effectiveMissCount;

    // default is 1?
    private double clockRate = 1;
    private double greatHitWindow;
    private double okHitWindow;
    private double mehHitWindow;
    private double overallDifficulty;
    private double approachRate;

    private Double speedDeviation;

    private int totalHits;
    private int totalSuccessfulHits;
    private int totalImperfectHits;

    protected OsuPerformanceAttributes CreatePerformanceAttributes(OsuApiScore score, BeatmapImpl attributes,
        boolean classic)
    {
        BeatmapImpl osuAttributes = attributes;
        usingClassicSliderAccuracy = classic;

        accuracy = OsuApiScore.getAccuracy(score.getCount300(), score.getCount100(), score.getCount50(), score.getCountMiss());
        scoreMaxCombo = score.getMaxCombo();
        countGreat = score.getCount300();
        countOk = score.getCount100();
        countMeh = score.getCount50();
        countMiss = score.getCountMiss();
        countSliderEndsDropped = 0; // v2 only attributes.SliderCount - score.Statistics.getOrDefault(HitResult.SliderTailHit, 0);
        countSliderTickMiss = 0; // v2 only score.Statistics.getOrDefault(HitResult.LargeTickMiss, 0);
        effectiveMissCount = countMiss;
        totalHits = countGreat + countOk + countMeh + countMiss;
        totalSuccessfulHits = countGreat + countOk + countMeh;
        totalImperfectHits = countOk + countMeh + countMiss;

        OsuHitWindows hitWindows = new OsuHitWindows();
        hitWindows.SetDifficulty(osuAttributes.OverallDifficulty());

        greatHitWindow = hitWindows.great;
        okHitWindow = hitWindows.ok;
        mehHitWindow = hitWindows.meh;

        overallDifficulty = osuAttributes.OverallDifficulty();
        approachRate = osuAttributes.approachRate();

        if (osuAttributes.SliderCount() > 0)
        {
            if (usingClassicSliderAccuracy)
            {
                // Consider that full combo is maximum combo minus dropped slider tails since they don't contribute to combo but also don't break it
                // In classic scores we can't know the amount of dropped sliders so we estimate to 10% of all sliders on the map
                double fullComboThreshold = attributes.MaxCombo() - 0.1 * osuAttributes.SliderCount();

                if (scoreMaxCombo < fullComboThreshold)
                    effectiveMissCount = fullComboThreshold / Math.max(1.0, scoreMaxCombo);

                // In classic scores there can't be more misses than a sum of all non-perfect judgements
                effectiveMissCount = Math.min(effectiveMissCount, totalImperfectHits);
            }
            else
            {
                double fullComboThreshold = attributes.MaxCombo() - countSliderEndsDropped;

                if (scoreMaxCombo < fullComboThreshold)
                    effectiveMissCount = fullComboThreshold / Math.max(1.0, scoreMaxCombo);

                // Combine regular misses with tick misses since tick misses break combo as well
                effectiveMissCount = Math.min(effectiveMissCount, countSliderTickMiss + countMiss);
            }
        }

        effectiveMissCount = Math.max(countMiss, effectiveMissCount);
        effectiveMissCount = Math.min(totalHits, effectiveMissCount);

        double multiplier = PERFORMANCE_BASE_MULTIPLIER;

        if (Mods.NoFail.is(score.getMods()))
            multiplier *= Math.max(0.90, 1.0 - 0.02 * effectiveMissCount);

        if (Mods.SpunOut.is(score.getMods()) && totalHits > 0)
            multiplier *= 1.0 - Math.pow((double)osuAttributes.SpinnerCount() / totalHits, 0.85);

        if (Mods.Relax.is(score.getMods()))
        {
            // https://www.desmos.com/calculator/bc9eybdthb
            // we use OD13.3 as maximum since it's the value at which great hitwidow becomes 0
            // this is well beyond currently maximum achievable OD which is 12.17 (DTx2 + DA with OD11)
            double okMultiplier = Math.max(0.0, overallDifficulty > 0.0 ? 1 - Math.pow(overallDifficulty / 13.33, 1.8) : 1.0);
            double mehMultiplier = Math.max(0.0, overallDifficulty > 0.0 ? 1 - Math.pow(overallDifficulty / 13.33, 5) : 1.0);

            // As we're adding Oks and Mehs to an approximated number of combo breaks the result can be higher than total hits in specific scenarios (which breaks some calculations) so we need to clamp it.
            effectiveMissCount = Math.min(effectiveMissCount + countOk * okMultiplier + countMeh * mehMultiplier, totalHits);
        }

        speedDeviation = calculateSpeedDeviation(osuAttributes);

        double aimValue = computeAimValue(score, osuAttributes);
        double speedValue = computeSpeedValue(score, osuAttributes);
        double accuracyValue = computeAccuracyValue(score, osuAttributes);
        double flashlightValue = computeFlashlightValue(score, osuAttributes);

        double totalValue =
                Math.pow(
                        Math.pow(aimValue, 1.1) +
                                Math.pow(speedValue, 1.1) +
                                Math.pow(accuracyValue, 1.1) +
                                Math.pow(flashlightValue, 1.1), 1.0 / 1.1
                ) * multiplier;

      return new OsuPerformanceAttributes(
          aimValue, speedValue, accuracyValue, flashlightValue, effectiveMissCount, speedDeviation, totalValue
      );
    }

    private double computeAimValue(OsuApiScore score, BeatmapImpl attributes)
    {
        if (Mods.Autoplay.is(score.getMods()))
            return 0.0;

        double aimDifficulty = attributes.AimDifficulty();

        if (attributes.SliderCount() > 0 && attributes.AimDifficultySliderCount() > 0)
        {
            double estimateImproperlyFollowedDifficultSliders;

            if (usingClassicSliderAccuracy)
            {
                // When the score is considered classic (regardless if it was made on old client or not) we consider all missing combo to be dropped difficult sliders
                int maximumPossibleDroppedSliders = totalImperfectHits;
                estimateImproperlyFollowedDifficultSliders = Math.min(Math.max(Math.min(maximumPossibleDroppedSliders, attributes.MaxCombo() - scoreMaxCombo), 0), attributes.AimDifficultySliderCount());
            }
            else
            {
                // We add tick misses here since they too mean that the player didn't follow the slider properly
                // We however aren't adding misses here because missing slider heads has a harsh penalty by itself and doesn't mean that the rest of the slider wasn't followed properly
                estimateImproperlyFollowedDifficultSliders = Math.min(Math.max(countSliderEndsDropped + countSliderTickMiss, 0), attributes.AimDifficultySliderCount());
            }

            double sliderNerfFactor = (1 - attributes.SliderFactor()) * Math.pow(1 - estimateImproperlyFollowedDifficultSliders / attributes.AimDifficultySliderCount(), 3) + attributes.SliderFactor();
            aimDifficulty *= sliderNerfFactor;
        }

        double aimValue = OsuStrainSkill.DifficultyToPerformance(aimDifficulty);

        double lengthBonus = 0.95 + 0.4 * Math.min(1.0, totalHits / 2000.0) +
                (totalHits > 2000 ? Math.log10(totalHits / 2000.0) * 0.5 : 0.0);
        aimValue *= lengthBonus;

        if (effectiveMissCount > 0)
            aimValue *= calculateMissPenalty(effectiveMissCount, attributes.AimDifficultyStrainCount());

        double approachRateFactor = 0.0;
        if (approachRate > 10.33)
            approachRateFactor = 0.3 * (approachRate - 10.33);
        else if (approachRate < 8.0)
            approachRateFactor = 0.05 * (8.0 - approachRate);

        if (Mods.Relax.is(score.getMods()))
            approachRateFactor = 0.0;

        aimValue *= 1.0 + approachRateFactor * lengthBonus; // Buff for longer maps with high AR.

        /*if (score.Mods.stream().anyMatch(m -> m instanceof OsuModBlinds))
            aimValue *= 1.3 + (totalHits * (0.0016 / (1 + 2 * effectiveMissCount)) * Math.pow(accuracy, 16)) * (1 - 0.003 * attributes.DrainRate * attributes.DrainRate);
        else*/ if (Mods.Hidden.is(score.getMods()) /* || "traceable" */)
        {
            // We want to give more reward for lower AR when it comes to AimDifficulty and HD. This nerfs high AR and buffs lower AR.
            aimValue *= 1.0 + 0.04 * (12.0 - approachRate);
        }

        aimValue *= accuracy;
        // It is important to consider accuracy difficulty when scaling with accuracy.
        aimValue *= 0.98 + Math.pow(Math.max(0, overallDifficulty), 2) / 2500;

        return aimValue;
    }

    private double computeSpeedValue(OsuApiScore score, BeatmapImpl attributes)
    {
        if (Mods.Relax.is(score.getMods()) || speedDeviation == null)
            return 0.0;

        double speedValue = OsuStrainSkill.DifficultyToPerformance(attributes.SpeedDifficulty());

        double lengthBonus = 0.95 + 0.4 * Math.min(1.0, totalHits / 2000.0) +
                (totalHits > 2000 ? Math.log10(totalHits / 2000.0) * 0.5 : 0.0);
        speedValue *= lengthBonus;

        if (effectiveMissCount > 0)
            speedValue *= calculateMissPenalty(effectiveMissCount, attributes.SpeedDifficultyStrainCount());

        double approachRateFactor = 0.0;
        if (approachRate > 10.33)
            approachRateFactor = 0.3 * (approachRate - 10.33);

        if (Mods.Autoplay.is(score.getMods()))
            approachRateFactor = 0.0;

        speedValue *= 1.0 + approachRateFactor * lengthBonus; // Buff for longer maps with high AR.

        /*if (score.Mods.stream().anyMatch(m -> m instanceof OsuModBlinds))
        {
            // Increasing the SpeedDifficulty value by object count for Blinds isn't ideal, so the minimum buff is given.
            speedValue *= 1.12;
        }
        else*/ if (Mods.Hidden.is(score.getMods()) /* || "traceable" */)
        {
            // We want to give more reward for lower AR when it comes to aim and HD. This nerfs high AR and buffs
            // lower AR.
            speedValue *= 1.0 + 0.04 * (12.0 - approachRate);
        }

        double speedHighDeviationMultiplier = calculateSpeedHighDeviationNerf(attributes);
        speedValue *= speedHighDeviationMultiplier;

        // Calculate accuracy assuming the worst case scenario
        double relevantTotalDiff = Math.max(0, totalHits - attributes.SpeedNoteCount());
        double relevantCountGreat = Math.max(0, countGreat - relevantTotalDiff);
        double relevantCountOk = Math.max(0, countOk - Math.max(0, relevantTotalDiff - countGreat));
        double relevantCountMeh = Math.max(0, countMeh - Math.max(0, relevantTotalDiff - countGreat - countOk));
        double relevantAccuracy = attributes.SpeedNoteCount() == 0 ? 0 : (relevantCountGreat * 6.0 + relevantCountOk * 2.0 + relevantCountMeh) / (attributes.SpeedNoteCount() * 6.0);

        // Scale the SpeedDifficulty value with accuracy and OD.
        speedValue *= (0.95 + Math.pow(Math.max(0, overallDifficulty), 2) / 750) * Math.pow((accuracy + relevantAccuracy) / 2.0, (14.5 - overallDifficulty) / 2);

        return speedValue;
    }

    private double computeAccuracyValue(OsuApiScore score, BeatmapImpl attributes)
    {
        if (Mods.Relax.is(score.getMods()))
            return 0.0;

        // This percentage only considers HitCircles of any value - in this part of the calculation we focus on hitting the timing hit window.
        double betterAccuracyPercentage;
        int amountHitObjectsWithAccuracy = attributes.HitCircleCount();
        if (!usingClassicSliderAccuracy)
            amountHitObjectsWithAccuracy += attributes.SliderCount();

        if (amountHitObjectsWithAccuracy > 0)
            betterAccuracyPercentage = ((countGreat - Math.max(totalHits - amountHitObjectsWithAccuracy, 0)) * 6 + countOk * 2 + countMeh) / (double)(amountHitObjectsWithAccuracy * 6);
        else
            betterAccuracyPercentage = 0;

        // It is possible to reach a negative accuracy with this formula. Cap it at zero - zero points.
        if (betterAccuracyPercentage < 0)
            betterAccuracyPercentage = 0;

        // Lots of arbitrary values from testing.
        // Considering to use derivation from perfect accuracy in a probabilistic manner - assume normal distribution.
        double accuracyValue = Math.pow(1.52163, overallDifficulty) * Math.pow(betterAccuracyPercentage, 24) * 2.83;

        // Bonus for many hitcircles - it's harder to keep good accuracy up for longer.
        accuracyValue *= Math.min(1.15, Math.pow(amountHitObjectsWithAccuracy / 1000.0, 0.3));

        // Increasing the accuracy value by object count for Blinds isn't ideal, so the minimum buff is given.
        /*if (score.Mods.stream().anyMatch(m -> m instanceof OsuModBlinds))
            accuracyValue *= 1.14;
        else*/ if (Mods.Hidden.is(score.getMods()) /* || "traceable" */)
            accuracyValue *= 1.08;

        if (Mods.Flashlight.is(score.getMods()))
            accuracyValue *= 1.02;

        return accuracyValue;
    }

    private double computeFlashlightValue(OsuApiScore score, BeatmapImpl attributes)
    {
        if (!Mods.Flashlight.is(score.getMods()))
            return 0.0;

        double flashlightValue = 0; // TODO Flashlight.DifficultyToPerformance(attributes.FlashlightDifficulty);

        // Penalize misses by assessing # of misses relative to the total # of objects. Default a 3% reduction for any # of misses.
        if (effectiveMissCount > 0)
            flashlightValue *= 0.97 * Math.pow(1 - Math.pow(effectiveMissCount / totalHits, 0.775), Math.pow(effectiveMissCount, .875));

        flashlightValue *= getComboScalingFactor(attributes);

        // Account for shorter maps having a higher ratio of 0 combo/100 combo flashlight radius.
        flashlightValue *= 0.7 + 0.1 * Math.min(1.0, totalHits / 200.0) +
                (totalHits > 200 ? 0.2 * Math.min(1.0, (totalHits - 200) / 200.0) : 0.0);

        // Scale the flashlight value with accuracy _slightly_.
        flashlightValue *= 0.5 + accuracy / 2.0;
        // It is important to also consider accuracy difficulty when doing that.
        flashlightValue *= 0.98 + Math.pow(Math.max(0, overallDifficulty), 2) / 2500;

        return flashlightValue;
    }

    /**
     * Estimates player's deviation on SpeedDifficulty notes using calculateDeviation(), assuming worst-case.
     * Treats all SpeedDifficulty notes as hit circles.
     */
    private Double calculateSpeedDeviation(BeatmapImpl attributes)
    {
        if (totalSuccessfulHits == 0)
            return null;

        // Calculate accuracy assuming the worst case scenario
        double speedNoteCount = attributes.SpeedNoteCount();
        speedNoteCount += (totalHits - attributes.SpeedNoteCount()) * 0.1;

        // Assume worst case: all mistakes were on speed notes
        double relevantCountMiss = Math.min(countMiss, speedNoteCount);
        double relevantCountMeh = Math.min(countMeh, speedNoteCount - relevantCountMiss);
        double relevantCountOk = Math.min(countOk, speedNoteCount - relevantCountMiss - relevantCountMeh);
        double relevantCountGreat = Math.max(0, speedNoteCount - relevantCountMiss - relevantCountMeh - relevantCountOk);

        return calculateDeviation(attributes, relevantCountGreat, relevantCountOk, relevantCountMeh, relevantCountMiss);
    }

    /**
     * Estimates the player's tap deviation based on the OD, given number of greats, oks, mehs and misses,
     * assuming the player's mean hit error is 0. The estimation is consistent in that two SS scores on the same map with the same settings
     * will always return the same deviation. Misses are ignored because they are usually due to misaiming.
     * Greats and oks are assumed to follow a normal distribution, whereas mehs are assumed to follow a uniform distribution.
     */
    private Double calculateDeviation(BeatmapImpl attributes, double relevantCountGreat, double relevantCountOk,
        double relevantCountMeh, double relevantCountMiss)
    {
        if (relevantCountGreat + relevantCountOk + relevantCountMeh <= 0)
            return null;

        double objectCount = relevantCountGreat + relevantCountOk + relevantCountMeh + relevantCountMiss;

        // The probability that a player hits a circle is unknown, but we can estimate it to be
        // the number of greats on circles divided by the number of circles, and then add one
        // to the number of circles as a bias correction.
        double n = Math.max(1, objectCount - relevantCountMiss - relevantCountMeh);
        final double z = 2.32634787404; // 99% critical value for the normal distribution (one-tailed).

        // Proportion of greats hit on circles, ignoring misses and 50s.
        double p = relevantCountGreat / n;

        // We can be 99% confident that p is at least this value.
        double pLowerBound = (n * p + z * z / 2) / (n + z * z) - z / (n + z * z) * Math.sqrt(n * p * (1 - p) + z * z / 4);

        // Compute the deviation assuming greats and oks are normally distributed, and mehs are uniformly distributed.
        // Begin with greats and oks first. Ignoring mehs, we can be 99% confident that the deviation is not higher than:
        double deviation = greatHitWindow / (Math.sqrt(2) * DifficultyCalculationUtils.ErfInv(pLowerBound));

        double randomValue = Math.sqrt(2 / Math.PI) * okHitWindow * Math.exp(-0.5 * Math.pow(okHitWindow / deviation, 2))
                / (deviation * DifficultyCalculationUtils.Erf(okHitWindow / (Math.sqrt(2) * deviation)));

        deviation *= Math.sqrt(1 - randomValue);

        // Value deviation approach as greatCount approaches 0
        double limitValue = okHitWindow / Math.sqrt(3);

        // If precision is not enough to compute true deviation - use limit value
        if (pLowerBound == 0 || randomValue >= 1 || deviation > limitValue)
            deviation = limitValue;

        // Then compute the variance for mehs.
        double mehVariance = (mehHitWindow * mehHitWindow + okHitWindow * mehHitWindow + okHitWindow * okHitWindow) / 3;

        // Find the total deviation.
        deviation = Math.sqrt(((relevantCountGreat + relevantCountOk) * Math.pow(deviation, 2) + relevantCountMeh * mehVariance) / (relevantCountGreat + relevantCountOk + relevantCountMeh));

        return deviation;
    }

    // Calculates multiplier for speed to account for improper tapping based on the deviation and speed difficulty
    // https://www.desmos.com/calculator/dmogdhzofn
    private double calculateSpeedHighDeviationNerf(BeatmapImpl attributes)
    {
        if (speedDeviation == null)
            return 0;

        double speedValue = OsuStrainSkill.DifficultyToPerformance(attributes.SpeedDifficulty());

        // Decides a point where the PP value achieved compared to the SpeedDifficulty deviation is assumed to be tapped improperly. Any PP above this point is considered "excess" SpeedDifficulty difficulty.
        // This is used to cause PP above the cutoff to scale logarithmically towards the original SpeedDifficulty value thus nerfing the value.
        double excessSpeedDifficultyCutoff = 100 + 220 * Math.pow(22 / speedDeviation, 6.5);

        if (speedValue <= excessSpeedDifficultyCutoff)
            return 1.0;

        final double scale = 50;
        double adjustedSpeedValue = scale * (Math.log((speedValue - excessSpeedDifficultyCutoff) / scale + 1) + excessSpeedDifficultyCutoff / scale);

        // 220 UR and less are considered tapped correctly to ensure that normal scores will be punished as little as possible
        double lerp = 1 - DifficultyCalculationUtils.ReverseLerp(speedDeviation, 22.0, 27.0);
        adjustedSpeedValue = adjustedSpeedValue * (1 - lerp) + speedValue * lerp;

        return adjustedSpeedValue / speedValue;
    }

    // Miss penalty assumes that a player will miss on the hardest parts of a map,
    // so we use the amount of relatively difficult sections to adjust miss penalty
    // to make it more punishing on maps with lower amount of hard sections.
    private double calculateMissPenalty(double missCount, double difficultStrainCount)
    {
        return 0.96 / ((missCount / (4 * Math.pow(Math.log(difficultStrainCount), 0.94))) + 1);
    }

    private double getComboScalingFactor(BeatmapImpl attributes)
    {
        return attributes.MaxCombo() <= 0 ? 1.0 : Math.min(Math.pow(scoreMaxCombo, 0.8) / Math.pow(attributes.MaxCombo(), 0.8), 1.0);
    }
}