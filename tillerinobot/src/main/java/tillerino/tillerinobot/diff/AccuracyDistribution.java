package tillerino.tillerinobot.diff;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static tillerino.tillerinobot.UserException.validateInclusiveBetween;

import org.apache.commons.lang3.tuple.Pair;
import org.tillerino.osuApiModel.OsuApiScore;
import tillerino.tillerinobot.UserException;

public record AccuracyDistribution(int x300, int x100, int x50, int miss) {
    /**
     * This will calculate the most precise approximation to hit the accuracy target. I.e. if you calculate accuracy
     * exactly based on 300s, 100s, 50s, and misses, this method will reproduce the original values.
     *
     * <p>Note that a user will most likely not input the precise accuracy value, but rather a rounded value. This will
     * often give an unexpected value for 50s. Since 50s are directly (not just indirectly through accuracy) in the
     * current pp formulas, this can lead to unexpected pp values.
     *
     * <p>Use {@link #model(int, int, double)} for a heuristic that will produce more predictable 50s.
     */
    public static AccuracyDistribution closest(int allObjects, int misses, double acc) {
        Pair<Integer, Integer> best = getBest300s(allObjects, misses, acc);
        int best300s = best.getLeft();
        int best100s = best.getRight();
        return new AccuracyDistribution(best300s, best100s, allObjects - best300s - best100s - misses, misses);
    }

    private static Pair<Integer, Integer> getBest300s(int allObjects, int misses, double acc) {
        acc = acc * allObjects * 6;
        int intAcc = (int) round(acc);
        int aomm = allObjects - misses;
        /*
         * Why does this work?
         *
         * _50s are uniquely determined by _300s and _100s.
         * => _50s becomes slack variable.
         * => Solution polytope is _300s + _100s <= allObjects - misses.
         * (Tip: draw this. It's 2D)
         * Figure out analytical, continuous solution.
         * => Discrete solution must be within +/- 1 * _300s, +/- 5 * _100s bounding box.
         * Make sure that 0 <= _300s <= allObjects - misses.
         *
         * After you've realized that, just mess with the equations until nothing is left
         */
        int guess300s = (int) round((acc - aomm) / 5);
        int best300s = 0;
        int best100s = 0;
        double bestError = Double.POSITIVE_INFINITY;
        for (int _300s = max(0, guess300s - 1); _300s <= min(aomm, guess300s + 1); _300s++) {
            int localAcc = min(2 * aomm + _300s * 4, max(aomm + _300s * 5, intAcc));
            int _100s = localAcc - (aomm + _300s * 5);
            double error = abs(acc - localAcc);
            if (error < bestError) {
                best300s = _300s;
                bestError = error;
                best100s = _100s;
            }
        }
        return Pair.of(best300s, best100s);
    }

    /**
     * Since 50s are punished directly in pp calculation (not just indirectly through accuracy), it is important that we
     * get a realistic number of 50s when turning an accuracy value into an {@link AccuracyDistribution}.
     *
     * <p>We fitted a polynomial to model the number of 50s based on the relative accuracy, which is the accuracy when
     * removing misses from the play.
     *
     * <p>Once the number of 50s has been determined, the 300s and 100s are chosen such that the accuracy is matched.
     * The accuracy that we can guarantee is +- 1/allObjects / 3, e.g. for 500 objects it is +- 0.0006 or 0.06%.
     */
    public static AccuracyDistribution model(int allObjects, int misses, final double acc) throws UserException {
        validateInclusiveBetween(1, Integer.MAX_VALUE, allObjects, "all object count");
        validateInclusiveBetween(0, allObjects, misses, "number of misses");
        // multiply acc by 100 for the error message
        validateInclusiveBetween(
                100 * OsuApiScore.getAccuracy(0, 0, allObjects - misses, misses),
                100 * OsuApiScore.getAccuracy(allObjects - misses, 0, 0, misses),
                100 * acc,
                "accuracy");
        // relative accuracy free of misses
        double racc = acc * (allObjects / ((double) allObjects - misses));

        double f50 = model50s(racc);
        int x50 = (int) Math.round(f50 * (allObjects - misses));
        int x300 = (int) Math.round((3 * racc / 2D + f50 / 4D - .5) * (allObjects - misses));
        int x100 = allObjects - misses - x300 - x50;
        assert x300 >= 0;
        assert x50 >= 0;
        assert x100 >= 0;
        return new AccuracyDistribution(x300, x100, x50, misses);
    }

    private static double model50s(double x) {
        return ((((-5.845461042897 * x + 24.1586850677877) * x - 38.565985189941) * x + 30.3957245438448) * x
                                - 12.5511536341837)
                        * x
                + 2.4081902553891;
    }
}
