package tillerino.tillerinobot.diff;

import javax.annotation.CheckForNull;

import org.tillerino.osuApiModel.types.BitwiseMods;

/**
 * Provides pp values for a beatmap played with a specified mod.
 * "PercentageEstimates" refers to the fact that the accuracy is specified and
 * that the value is estimated although the value is usually quite accurate.
 */
public interface PercentageEstimates {
	public double getPPForAcc(double acc);

	public double getPP(double acc, int combo, int misses);

	@BitwiseMods
	long getMods();

	boolean isShaky();

	@CheckForNull
	Double getStarDiff();

	boolean isOppaiOnly();

	boolean isRanked();
}