package tillerino.tillerinobot.diff;

import static org.tillerino.osuApiModel.Mods.Autoplay;
import static org.tillerino.osuApiModel.Mods.Flashlight;
import static org.tillerino.osuApiModel.Mods.Hidden;
import static org.tillerino.osuApiModel.Mods.NoFail;
import static org.tillerino.osuApiModel.Mods.Relax;
import static org.tillerino.osuApiModel.Mods.Relax2;
import static org.tillerino.osuApiModel.Mods.SpunOut;
import static tillerino.tillerinobot.diff.MathHelper.Clamp;
import static tillerino.tillerinobot.diff.MathHelper.log10;
import static tillerino.tillerinobot.diff.MathHelper.pow;
import static tillerino.tillerinobot.diff.MathHelper.static_cast_f32;
import static tillerino.tillerinobot.diff.MathHelper.std_pow;
import static tillerino.tillerinobot.diff.MathHelper.std_min;
import static tillerino.tillerinobot.diff.MathHelper.std_max;

import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.types.BitwiseMods;

/**
 * This class is a direct translation of OsuScore.cpp, the original code
 * to compute the pp for given play, aim and speed values of an osu standard
 * score:
 * https://github.com/ppy/osu-performance/blob/2022.929.0/src/performance/osu/OsuScore.cpp
 * <hr>
 * This file violates all Java coding standards to be as easily comparable to
 * the original as possible.
 */
// suppress all found Sonar warnings, since we are trying to copy C++ code
@SuppressWarnings({ "squid:S00116", "squid:S00117", "squid:ClassVariableVisibilityCheck", "squid:S00100" })
public class OsuScore {
	private final int _maxCombo;
	private final int _num300;
	private final int _num100;
	private final int _num50;
	private final int _numMiss;
	@BitwiseMods
	private final long _mods;
	
	public OsuScore(int m_MaxCombo, int m_Amount300, int m_Amount100,
			int m_Amount50, int m_AmountMiss, @BitwiseMods long m_Mods) {
		super();
		this._maxCombo = m_MaxCombo;
		this._num300 = m_Amount300;
		this._num100 = m_Amount100;
		this._num50 = m_Amount50;
		this._numMiss = m_AmountMiss;
		this._mods = m_Mods;
	}

	public OsuScore(OsuApiScore score) {
		this(score.getMaxCombo(), score.getCount300(), score.getCount100(),
				score.getCount50(), score.getCountMiss(), score.getMods());
	}

	private float _totalValue;
	private float _aimValue;
	private float _accuracyValue;
	private float _speedValue;
	private float _flashlightValue;
	private float _effectiveMissCount;
	
public float getPP(Beatmap beatmap){
	computeEffectiveMissCount(beatmap);

	computeAimValue(beatmap);
	computeSpeedValue(beatmap);
	computeAccuracyValue(beatmap);
	computeFlashlightValue(beatmap);

	computeTotalValue(beatmap);

	return TotalValue();
}

float TotalValue()
{
	return _totalValue;
}

float Accuracy()
{
	if (TotalHits() == 0)
		return 0;

	return Clamp(
		static_cast_f32(_num50 * 50 + _num100 * 100 + _num300 * 300) / (TotalHits() * 300), 0.0f, 1.0f);
}

int TotalHits()
{
	return _num50 + _num100 + _num300 + _numMiss;
}

int TotalSuccessfulHits()
{
	return _num50 + _num100 + _num300;
}

void computeEffectiveMissCount(Beatmap beatmap)
{
	// guess the number of misses + slider breaks from combo
	float comboBasedMissCount = 0.0f;
	float beatmapMaxCombo = beatmap.DifficultyAttribute(_mods, Beatmap.MaxCombo);
	if (beatmap.NumSliders() > 0)
	{
		float fullComboThreshold = beatmapMaxCombo - 0.1f * beatmap.NumSliders();
		if (_maxCombo < fullComboThreshold)
			comboBasedMissCount = fullComboThreshold / std_max(1, _maxCombo);
	}

	// Clamp miss count to maximum amount of possible breaks
	comboBasedMissCount = std_min(comboBasedMissCount, static_cast_f32(_num100 + _num50 + _numMiss));

	_effectiveMissCount = std_max(static_cast_f32(_numMiss), comboBasedMissCount);
}

void computeTotalValue(Beatmap beatmap)
{
	// Don't count scores made with supposedly unranked mods
	if (Relax.is(_mods) ||
		Relax2.is(_mods) ||
		Autoplay.is(_mods))
	{
		_totalValue = 0;
		return;
	}

	float multiplier = 1.14f; // This is being adjusted to keep the final pp value scaled around what it used to be when changing things.

	if (NoFail.is(_mods))
		multiplier *= std_max(0.9f, 1.0f - 0.02f * _effectiveMissCount);

	int numTotalHits = TotalHits();
	if (SpunOut.is(_mods))
		multiplier *= 1.0f - std_pow(beatmap.NumSpinners() / static_cast_f32(numTotalHits), 0.85f);

	_totalValue =
		std_pow(
			std_pow(_aimValue, 1.1f) +
				std_pow(_speedValue, 1.1f) +
				std_pow(_accuracyValue, 1.1f) +
				std_pow(_flashlightValue, 1.1f),
			1.0f / 1.1f) *
		multiplier;
}

void computeAimValue(Beatmap beatmap)
{
	_aimValue = pow(5.0f * std_max(1.0f, beatmap.DifficultyAttribute(_mods, Beatmap.Aim) / 0.0675f) - 4.0f, 3.0f) / 100000.0f;

	int numTotalHits = TotalHits();

	float lengthBonus = 0.95f + 0.4f * std_min(1.0f, static_cast_f32(numTotalHits) / 2000.0f) +
					  (numTotalHits > 2000 ? log10(static_cast_f32(numTotalHits) / 2000.0f) * 0.5f : 0.0f);
	_aimValue *= lengthBonus;

	// Penalize misses by assessing # of misses relative to the total # of objects. Default a 3% reduction for any # of misses.
	if (_effectiveMissCount > 0)
		_aimValue *= 0.97f * std_pow(1.0f - std_pow(_effectiveMissCount / static_cast_f32(numTotalHits), 0.775f), _effectiveMissCount);

	_aimValue *= getComboScalingFactor(beatmap);

	float approachRate = beatmap.DifficultyAttribute(_mods, Beatmap.AR);
	float approachRateFactor = 0.0f;
	if (approachRate > 10.33f)
		approachRateFactor = 0.3f * (approachRate - 10.33f);
	else if (approachRate < 8.0f)
		approachRateFactor = 0.05f * (8.0f - approachRate);

	_aimValue *= 1.0f + approachRateFactor * lengthBonus;

	// We want to give more reward for lower AR when it comes to aim and HD. This nerfs high AR and buffs lower AR.
	if (Hidden.is(_mods))
		_aimValue *= 1.0f + 0.04f * (12.0f - approachRate);

	// We assume 15% of sliders in a map are difficult since there's no way to tell from the performance calculator.
	float estimateDifficultSliders = beatmap.NumSliders() * 0.15f;

	if (beatmap.NumSliders() > 0)
	{
		float maxCombo = beatmap.DifficultyAttribute(_mods, Beatmap.MaxCombo);
		float estimateSliderEndsDropped = std_min(std_max(std_min(static_cast_f32(_num100 + _num50 + _numMiss), maxCombo - _maxCombo), 0.0f), estimateDifficultSliders);
		float sliderFactor = beatmap.DifficultyAttribute(_mods, Beatmap.SliderFactor);
		float sliderNerfFactor = (1.0f - sliderFactor) * std_pow(1.0f - estimateSliderEndsDropped / estimateDifficultSliders, 3) + sliderFactor;
		_aimValue *= sliderNerfFactor;
	}

	_aimValue *= Accuracy();
	// It is important to consider accuracy difficulty when scaling with accuracy.
	_aimValue *= 0.98f + (pow(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 2) / 2500);
}

void computeSpeedValue(Beatmap beatmap)
{
	_speedValue = pow(5.0f * std_max(1.0f, beatmap.DifficultyAttribute(_mods, Beatmap.Speed) / 0.0675f) - 4.0f, 3.0f) / 100000.0f;

	int numTotalHits = TotalHits();

	float lengthBonus = 0.95f + 0.4f * std_min(1.0f, static_cast_f32(numTotalHits) / 2000.0f) +
		(numTotalHits > 2000 ? log10(static_cast_f32(numTotalHits) / 2000.0f) * 0.5f : 0.0f);
	_speedValue *= lengthBonus;

	// Penalize misses by assessing # of misses relative to the total # of objects. Default a 3% reduction for any # of misses.
	if (_effectiveMissCount > 0)
		_speedValue *= 0.97f * std_pow(1.0f - std_pow(_effectiveMissCount / static_cast_f32(numTotalHits), 0.775f), std_pow(_effectiveMissCount, 0.875f));

	_speedValue *= getComboScalingFactor(beatmap);

	float approachRate = beatmap.DifficultyAttribute(_mods, Beatmap.AR);
	float approachRateFactor = 0.0f;
	if (approachRate > 10.33f)
		approachRateFactor = 0.3f * (approachRate - 10.33f);

	_speedValue *= 1.0f + approachRateFactor * lengthBonus; // Buff for longer maps with high AR.

	// We want to give more reward for lower AR when it comes to speed and HD. This nerfs high AR and buffs lower AR.
	if (Hidden.is(_mods))
		_speedValue *= 1.0f + 0.04f * (12.0f - approachRate);

	// Calculate accuracy assuming the worst case scenario
	float relevantTotalDiff = static_cast_f32(numTotalHits) - beatmap.DifficultyAttribute(_mods, Beatmap.SpeedNoteCount);
	float relevantCountGreat = std_max(0.0f, _num300 - relevantTotalDiff);
	float relevantCountOk = std_max(0.0f, _num100 - std_max(0.0f, relevantTotalDiff - _num300));
	float relevantCountMeh = std_max(0.0f, _num50 - std_max(0.0f, relevantTotalDiff - _num300 - _num100));
	float relevantAccuracy = beatmap.DifficultyAttribute(_mods, Beatmap.SpeedNoteCount) == 0.0f ? 0.0f : (relevantCountGreat * 6.0f + relevantCountOk * 2.0f + relevantCountMeh) / (beatmap.DifficultyAttribute(_mods, Beatmap.SpeedNoteCount) * 6.0f);

	// Scale the speed value with accuracy and OD.
	_speedValue *= (0.95f + std_pow(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 2) / 750) * std_pow((Accuracy() + relevantAccuracy) / 2.0f, (14.5f - std_max(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 8.0f)) / 2);

	// Scale the speed value with # of 50s to punish doubletapping.
	_speedValue *= std_pow(0.99f, _num50 < numTotalHits / 500.0f ? 0.0f : _num50 - numTotalHits / 500.0f);
}

void computeAccuracyValue(Beatmap beatmap)
{
	// This percentage only considers HitCircles of any value - in this part of the calculation we focus on hitting the timing hit window.
	float betterAccuracyPercentage;

	int numHitObjectsWithAccuracy;
	if (beatmap.ScoreVersion() == Beatmap.EScoreVersion.ScoreV2)
	{
		numHitObjectsWithAccuracy = TotalHits();
		betterAccuracyPercentage = Accuracy();
	}
	// Either ScoreV1 or some unknown value. Let's default to previous behavior.
	else
	{
		numHitObjectsWithAccuracy = beatmap.NumHitCircles();
		if (numHitObjectsWithAccuracy > 0)
			betterAccuracyPercentage = static_cast_f32((_num300 - (TotalHits() - numHitObjectsWithAccuracy)) * 6 + _num100 * 2 + _num50) / (numHitObjectsWithAccuracy * 6);
		else
			betterAccuracyPercentage = 0;

		// It is possible to reach a negative accuracy with this formula. Cap it at zero - zero points.
		if (betterAccuracyPercentage < 0)
			betterAccuracyPercentage = 0;
	}

	// Lots of arbitrary values from testing.
	// Considering to use derivation from perfect accuracy in a probabilistic manner - assume normal distribution.
	_accuracyValue =
			pow(1.52163f, beatmap.DifficultyAttribute(_mods, Beatmap.OD)) * pow(betterAccuracyPercentage, 24) *
					2.83f;

	// Bonus for many hitcircles - it's harder to keep good accuracy up for longer.
	_accuracyValue *= std_min(1.15f, static_cast_f32(pow(numHitObjectsWithAccuracy / 1000.0f, 0.3f)));

	if (Hidden.is(_mods))
		_accuracyValue *= 1.08f;

	if (Flashlight.is(_mods))
		_accuracyValue *= 1.02f;
}

void computeFlashlightValue(Beatmap beatmap)
{
	_flashlightValue = 0.0f;

	if (!Flashlight.is(_mods))
		return;

	_flashlightValue = std_pow(beatmap.DifficultyAttribute(_mods, Beatmap.Flashlight), 2.0f) * 25.0f;

	int numTotalHits = TotalHits();

	// Penalize misses by assessing # of misses relative to the total # of objects. Default a 3% reduction for any # of misses.
	if (_effectiveMissCount > 0)
		_flashlightValue *= 0.97f * std_pow(1 - std_pow(_effectiveMissCount / static_cast_f32(numTotalHits), 0.775f), std_pow(_effectiveMissCount, 0.875f));

	_flashlightValue *= getComboScalingFactor(beatmap);

	// Account for shorter maps having a higher ratio of 0 combo/100 combo flashlight radius.
	_flashlightValue *= 0.7f + 0.1f * std_min(1.0f, static_cast_f32(numTotalHits) / 200.0f) +
		(numTotalHits > 200 ? 0.2f * std_min(1.0f, (static_cast_f32(numTotalHits) - 200) / 200.0f) : 0.0f);

	// Scale the flashlight value with accuracy _slightly_.
	_flashlightValue *= 0.5f + Accuracy() / 2.0f;
	// It is important to also consider accuracy difficulty when doing that.
	_flashlightValue *= 0.98f + std_pow(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 2.0f) / 2500.0f;
}

float getComboScalingFactor(Beatmap beatmap)
{
	float maxCombo = beatmap.DifficultyAttribute(_mods, Beatmap.MaxCombo);
	if (maxCombo > 0)
		return std_min(static_cast_f32(pow(_maxCombo, 0.8f) / pow(maxCombo, 0.8f)), 1.0f);
	return 1.0f;
}
}
