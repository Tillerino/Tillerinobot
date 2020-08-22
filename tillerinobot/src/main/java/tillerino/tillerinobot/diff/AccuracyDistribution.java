package tillerino.tillerinobot.diff;

import lombok.Value;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import org.apache.commons.lang3.tuple.Pair;

@Value
public class AccuracyDistribution {
	int x300;
	int x100;
	int x50;
	int miss;

	public static AccuracyDistribution get(int allObjects, int misses, double acc) {
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
}
