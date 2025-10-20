package tillerino.tillerinobot.diff;

import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.UserException;

/**
 * Provides pp values for a beatmap played with a specified mod. "PercentageEstimates" refers to the fact that the
 * accuracy is specified and that the value is estimated although the value is usually quite accurate.
 */
public interface PercentageEstimates {
    public double getPP(double acc);

    public double getPP(double acc, int combo, int misses) throws UserException;

    public double getPP(int x100, int x50, int combo, int misses);

    @BitwiseMods
    long getMods();

    double getStarDiff();

    double getApproachRate();

    double getOverallDifficulty();
}
