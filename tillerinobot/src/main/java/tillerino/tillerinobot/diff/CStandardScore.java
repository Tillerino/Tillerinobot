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
import static tillerino.tillerinobot.diff.MathHelper.clamp;
import static tillerino.tillerinobot.diff.MathHelper.static_cast;

import org.tillerino.osuApiModel.OsuApiScore;
import org.tillerino.osuApiModel.types.BitwiseMods;

/**
 * This class is a direct translation of CStandardScore.cpp, the original code
 * to compute the pp for given play, aim and speed values of a osu standard
 * score. It can be found here: https://github.com/ppy/osu-performance.git.
 * 
 * This version is based on the commit 6890a5d6151e1c0bb5af438b0fd0079eebb26306.
 * 
 * This file violates all Java coding standards to be as easily comparable to
 * the original as possible.
 */
// suppress all found Sonar warnings, since we are trying to copy C# code
@SuppressWarnings({ "squid:S00116", "squid:S00117", "squid:ClassVariableVisibilityCheck", "squid:S00100" })
public class CStandardScore {
	private final int _maxCombo;
	private final int _amount300;
	private final int _amount100;
	private final int _amount50;
	private final int _amountMiss;
	@BitwiseMods
	private final long _mods;
	
	public CStandardScore(int m_MaxCombo, int m_Amount300, int m_Amount100,
			int m_Amount50, int m_AmountMiss, @BitwiseMods long m_Mods) {
		super();
		this._maxCombo = m_MaxCombo;
		this._amount300 = m_Amount300;
		this._amount100 = m_Amount100;
		this._amount50 = m_Amount50;
		this._amountMiss = m_AmountMiss;
		this._mods = m_Mods;
	}

	public CStandardScore(OsuApiScore score) {
		this(score.getMaxCombo(), score.getCount300(), score.getCount100(),
				score.getCount50(), score.getCountMiss(), score.getMods());
	}

	public double _totalValue;
	public double _aimValue;
	public double _accValue;
	public double _speedValue;
	
public double getPP(CBeatmap Beatmap){
	ComputeAimValue(Beatmap);
	ComputeSpeedValue(Beatmap);
	ComputeAccValue(Beatmap);

	ComputeTotalValue();
	
	return TotalValue();
}

double TotalValue()
{
	return _totalValue;
}

double Accuracy()
{
	if(TotalHits() == 0)
	{
		return 0;
	}

	return clamp(static_cast(_amount50 * 50 + _amount100 * 100 + _amount300 * 300)
				 / (TotalHits() * 300), 0.0f, 1.0f);
}

int TotalHits()
{
	return _amount50 + _amount100 + _amount300 + _amountMiss;
}

int TotalSuccessfulHits()
{
	return _amount50 + _amount100 + _amount300;
}

void ComputeTotalValue()
{
	// Don't count scores made with supposedly unranked mods
	if(Relax.is(_mods) ||
	   Relax2.is(_mods) ||
	   Autoplay.is(_mods))
	{
		_totalValue = 0;
		return;
	}


	// Custom multipliers for NoFail and SpunOut.
	double multiplier = 1.12f; // This is being adjusted to keep the final pp value scaled around what it used to be when changing things

	if(NoFail.is(_mods))
	{
		multiplier *= 0.90f;
	}

	if(SpunOut.is(_mods))
	{
		multiplier *= 0.95f;
	}

	_totalValue =
		pow(
			pow(_aimValue, 1.1f) +
			pow(_speedValue, 1.1f) +
			pow(_accValue, 1.1f), 1.0f / 1.1f
		) * multiplier;
}

void ComputeAimValue(CBeatmap beatmap)
{
	double rawAim = beatmap.DifficultyAttribute(_mods, CBeatmap.Aim);

	if(TouchDevice.is(_mods))
		rawAim = pow(rawAim, 0.8f);

	_aimValue = pow(5.0f * max(1.0f, rawAim / 0.0675f) - 4.0f, 3.0f) / 100000.0f;

	int amountTotalHits = TotalHits();

	// Longer maps are worth more
	double LengthBonus = 0.95f + 0.4f * min(1.0f, static_cast(amountTotalHits) / 2000.0f) +
		(amountTotalHits > 2000 ? log10(static_cast(amountTotalHits) / 2000.0f) * 0.5f : 0.0f);

	_aimValue *= LengthBonus;

	// Penalize misses exponentially. This mainly fixes tag4 maps and the likes until a per-hitobject solution is available
	_aimValue *= pow(0.97f, _amountMiss);

	// Combo scaling
	double maxCombo = beatmap.DifficultyAttribute(_mods, CBeatmap.MaxCombo);
	if(maxCombo > 0)
	{
		_aimValue *=
			min(pow(static_cast(_maxCombo), 0.8f) / pow(maxCombo, 0.8f), 1.0f);
	}
	
	double approachRate = beatmap.DifficultyAttribute(_mods, CBeatmap.AR);
	double approachRateFactor = 1.0f;
	if(approachRate > 10.33f)
	{
		approachRateFactor += 0.45f * (approachRate - 10.33f);
	}
	else if(approachRate < 8.0f)
	{
		// HD is worth more with lower ar!
		if(Hidden.is(_mods))
		{
			approachRateFactor += 0.02f * (8.0f - approachRate);
		}
		else
		{
			approachRateFactor += 0.01f * (8.0f - approachRate);
		}
	}

	_aimValue *= approachRateFactor;

	if(Hidden.is(_mods))
	{
		_aimValue *= 1.18f;
	}

	if(Flashlight.is(_mods))
	{
		// Apply length bonus again if flashlight is on simply because it becomes a lot harder on longer maps.
		_aimValue *= 1.45f * LengthBonus;
	}

	// Scale the aim value with accuracy _slightly_
	_aimValue *= 0.5f + Accuracy() / 2.0f;
	// It is important to also consider accuracy difficulty when doing that
	_aimValue *= 0.98f + (pow(beatmap.DifficultyAttribute(_mods, CBeatmap.OD) , 2) / 2500);
}

void ComputeSpeedValue(CBeatmap beatmap)
{
	_speedValue = pow(5.0f * max(1.0f, beatmap.DifficultyAttribute(_mods, CBeatmap.Speed) / 0.0675f) - 4.0f, 3.0f) / 100000.0f;


	int amountTotalHits = TotalHits();

	// Longer maps are worth more
	_speedValue *=
		0.95f + 0.4f * min(1.0f, static_cast(amountTotalHits) / 2000.0f) +
		(amountTotalHits > 2000 ? log10(static_cast(amountTotalHits) / 2000.0f) * 0.5f : 0.0f);

	// Penalize misses exponentially. This mainly fixes tag4 maps and the likes until a per-hitobject solution is available
	_speedValue *= pow(0.97f, _amountMiss);


	// Combo scaling
	double maxCombo = beatmap.DifficultyAttribute(_mods, CBeatmap.MaxCombo);
	if(maxCombo > 0)
	{
		_speedValue *=
			min(static_cast(pow(_maxCombo, 0.8f) / pow(maxCombo, 0.8f)), 1.0f);
	}
	

	// Scale the speed value with accuracy _slightly_
	_speedValue *= 0.5f + Accuracy() / 2.0f;
	// It is important to also consider accuracy difficulty when doing that
	_speedValue *= 0.98f + (pow(beatmap.DifficultyAttribute(_mods, CBeatmap.OD), 2) / 2500);
}

void ComputeAccValue(CBeatmap beatmap)
{
	// This percentage only considers HitCircles of any value - in this part of the calculation we focus on hitting the timing hit window
	double betterAccuracyPercentage;

	double amountHitObjectsWithAccuracy;
	if(beatmap.ScoreVersion() == CBeatmap.EScoreVersion.ScoreV2)
	{
		amountHitObjectsWithAccuracy = TotalHits();
		betterAccuracyPercentage = Accuracy();
	}
	// Either ScoreV1 or some unknown value. Let's default to previous behavior.
	else
	{
		amountHitObjectsWithAccuracy = beatmap.AmountHitCircles();
		if(amountHitObjectsWithAccuracy > 0)
		{
			betterAccuracyPercentage =
				((_amount300 - (TotalHits() - amountHitObjectsWithAccuracy)) * 6 + _amount100 * 2 + _amount50) / (amountHitObjectsWithAccuracy * 6);
		}
		else
		{
			betterAccuracyPercentage = 0;
		}

		// It is possible to reach a negative accuracy with this formula. Cap it at zero - zero points
		if(betterAccuracyPercentage < 0)
		{
			betterAccuracyPercentage = 0;
		}
	}

	// Lots of arbitrary values from testing.
	// Considering to use derivation from perfect accuracy in a probabilistic manner - assume normal distribution
	_accValue =
		pow(1.52163f, beatmap.DifficultyAttribute(_mods, CBeatmap.OD)) * pow(betterAccuracyPercentage, 24) *
		2.83f;

	// Bonus for many hitcircles - it's harder to keep good accuracy up for longer
	_accValue *= min(1.15f, static_cast(pow(amountHitObjectsWithAccuracy / 1000.0f, 0.3f)));

	if(Hidden.is(_mods))
	{
		_accValue *= 1.02f;
	}

	if(Flashlight.is(_mods))
	{
		_accValue *= 1.02f;
	}
}
}
