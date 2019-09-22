package tillerino.tillerinobot.diff;

import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static org.tillerino.osuApiModel.Mods.Autoplay;
import static org.tillerino.osuApiModel.Mods.Flashlight;
import static org.tillerino.osuApiModel.Mods.Hidden;
import static org.tillerino.osuApiModel.Mods.NoFail;
import static org.tillerino.osuApiModel.Mods.Relax;
import static org.tillerino.osuApiModel.Mods.Relax2;
import static org.tillerino.osuApiModel.Mods.SpunOut;
import static org.tillerino.osuApiModel.Mods.TouchDevice;
import static tillerino.tillerinobot.diff.MathHelper.Clamp;
import static tillerino.tillerinobot.diff.MathHelper.static_cast;

import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.types.BitwiseMods;

/**
 * This class is a direct translation of OsuScore.cpp, the original code
 * to compute the pp for given play, aim and speed values of a osu standard
 * score: 
 * 
 * https://github.com/ppy/osu-performance/blob/a7e98ebea5bcb51c7f3463d8cc45ab15256f636e/src/performance/osu/OsuScore.cpp
 * 
 * This file violates all Java coding standards to be as easily comparable to
 * the original as possible.
 * 
 */
// suppress all found Sonar warnings, since we are trying to copy C# code
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

	public double _totalValue;
	public double _aimValue;
	public double _accValue;
	public double _speedValue;
	
public double getPP(Beatmap beatmap){
	computeAimValue(beatmap);
	computeSpeedValue(beatmap);
	computeAccValue(beatmap);

	computeTotalValue();

	return TotalValue();
}

double TotalValue()
{
	return _totalValue;
}

double Accuracy()
{
	if (TotalHits() == 0)
		return 0;

	return Clamp(
		static_cast(_num50 * 50 + _num100 * 100 + _num300 * 300) / (TotalHits() * 300), 0.0f, 1.0f
	);
}

int TotalHits()
{
	return _num50 + _num100 + _num300 + _numMiss;
}

int TotalSuccessfulHits()
{
	return _num50 + _num100 + _num300;
}

void computeTotalValue()
{
	// Don't count scores made with supposedly unranked mods
	if (Relax.is(_mods) ||
		Relax2.is(_mods) ||
		Autoplay.is(_mods))
	{
		_totalValue = 0;
		return;
	}

	// Custom multipliers for NoFail and SpunOut.
	double multiplier = 1.12f; // This is being adjusted to keep the final pp value scaled around what it used to be when changing things

	if (NoFail.is(_mods))
		multiplier *= 0.90f;

	if (SpunOut.is(_mods))
		multiplier *= 0.95f;

	_totalValue =
		pow(
			pow(_aimValue, 1.1f) +
			pow(_speedValue, 1.1f) +
			pow(_accValue, 1.1f), 1.0f / 1.1f
		) * multiplier;
}

void computeAimValue(Beatmap beatmap)
{
	double rawAim = beatmap.DifficultyAttribute(_mods, Beatmap.Aim);

	if (TouchDevice.is(_mods))
		rawAim = pow(rawAim, 0.8f);

	_aimValue = pow(5.0f * max(1.0f, rawAim / 0.0675f) - 4.0f, 3.0f) / 100000.0f;

	int numTotalHits = TotalHits();

	// Longer maps are worth more
	double LengthBonus = 0.95f + 0.4f * min(1.0f, static_cast(numTotalHits) / 2000.0f) +
		(numTotalHits > 2000 ? log10(static_cast(numTotalHits) / 2000.0f) * 0.5f : 0.0f);

	_aimValue *= LengthBonus;

	// Penalize misses exponentially. This mainly fixes tag4 maps and the likes until a per-hitobject solution is available
	_aimValue *= pow(0.97f, _numMiss);

	// Combo scaling
	double maxCombo = beatmap.DifficultyAttribute(_mods, Beatmap.MaxCombo);
	if (maxCombo > 0)
		_aimValue *= min(static_cast(pow(_maxCombo, 0.8f) / pow(maxCombo, 0.8f)), 1.0f);

	double approachRate = beatmap.DifficultyAttribute(_mods, Beatmap.AR);
	double approachRateFactor = 1.0f;
	if (approachRate > 10.33f)
		approachRateFactor += 0.3f * (approachRate - 10.33f);
	else if (approachRate < 8.0f)
	{
		approachRateFactor += 0.01f * (8.0f - approachRate);
	}

	_aimValue *= approachRateFactor;

	// We want to give more reward for lower AR when it comes to aim and HD. This nerfs high AR and buffs lower AR.
	if (Hidden.is(_mods))
		_aimValue *= 1.0f + 0.04f * (12.0f - approachRate);
	
	if (Flashlight.is(_mods))
		// Apply object-based bonus for flashlight.
		_aimValue *= 1.0f + 0.35f * min(1.0f, static_cast(numTotalHits) / 200.0f) +
         		(numTotalHits > 200 ? 0.3f * min(1.0f, static_cast(numTotalHits - 200) / 300.0f) +
         		(numTotalHits > 500 ? static_cast(numTotalHits - 500) / 1200.0f : 0.0f) : 0.0f);

	// Scale the aim value with accuracy _slightly_
	_aimValue *= 0.5f + Accuracy() / 2.0f;
	// It is important to also consider accuracy difficulty when doing that
	_aimValue *= 0.98f + (pow(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 2) / 2500);
}

void computeSpeedValue(Beatmap beatmap)
{
	_speedValue = pow(5.0f * max(1.0f, beatmap.DifficultyAttribute(_mods, Beatmap.Speed) / 0.0675f) - 4.0f, 3.0f) / 100000.0f;

	int numTotalHits = TotalHits();

	double approachRate = beatmap.DifficultyAttribute(_mods, Beatmap.AR);
	double approachRateFactor = 1.0f;
	if (approachRate > 10.33f)
		approachRateFactor += 0.3f * (approachRate - 10.33f);

	_speedValue *= approachRateFactor;
	
	// Longer maps are worth more
	_speedValue *=
		0.95f + 0.4f * min(1.0f, static_cast(numTotalHits) / 2000.0f) +
		(numTotalHits > 2000 ? log10(static_cast(numTotalHits) / 2000.0f) * 0.5f : 0.0f);

	// Penalize misses exponentially. This mainly fixes tag4 maps and the likes until a per-hitobject solution is available
	_speedValue *= pow(0.97f, _numMiss);

	// Combo scaling
	double maxCombo = beatmap.DifficultyAttribute(_mods, Beatmap.MaxCombo);
	if(maxCombo > 0)
		_speedValue *= min(static_cast(pow(_maxCombo, 0.8f) / pow(maxCombo, 0.8f)), 1.0f);

	// We want to give more reward for lower AR when it comes to speed and HD. This nerfs high AR and buffs lower AR.
	if (Hidden.is(_mods))
		_speedValue *= 1.0f + 0.04f * (12.0f - approachRate);

	// Scale the speed value with accuracy _slightly_
	_speedValue *= 0.02f + Accuracy();
	// It is important to also consider accuracy difficulty when doing that
	_speedValue *= 0.96f + (pow(beatmap.DifficultyAttribute(_mods, Beatmap.OD), 2) / 1600);
}

void computeAccValue(Beatmap beatmap)
{
	// This percentage only considers HitCircles of any value - in this part of the calculation we focus on hitting the timing hit window
	double betterAccuracyPercentage;

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
			betterAccuracyPercentage = static_cast((_num300 - (TotalHits() - numHitObjectsWithAccuracy)) * 6 + _num100 * 2 + _num50) / (numHitObjectsWithAccuracy * 6);
		else
			betterAccuracyPercentage = 0;

		// It is possible to reach a negative accuracy with this formula. Cap it at zero - zero points
		if (betterAccuracyPercentage < 0)
			betterAccuracyPercentage = 0;
	}

	// Lots of arbitrary values from testing.
	// Considering to use derivation from perfect accuracy in a probabilistic manner - assume normal distribution
	_accValue =
		pow(1.52163f, beatmap.DifficultyAttribute(_mods, Beatmap.OD)) * pow(betterAccuracyPercentage, 24) *
		2.83f;

	// Bonus for many hitcircles - it's harder to keep good accuracy up for longer
	_accValue *= min(1.15f, static_cast(pow(numHitObjectsWithAccuracy / 1000.0f, 0.3f)));

	if (Hidden.is(_mods))
		_accValue *= 1.08f;

	if (Flashlight.is(_mods))
		_accValue *= 1.02f;
}
}
