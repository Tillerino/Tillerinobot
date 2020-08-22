package tillerino.tillerinobot.diff;

import lombok.Value;

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

	private static Pair<Integer, Double> getBest100s(int allObjects, int x300, int misses, double acc) {
		int best = Math.min(2 * (allObjects - misses) + x300 * 4, Math.max(allObjects - misses + x300 * 5, (int) Math.round(acc * allObjects * 6)));
		return Pair.of(best - (allObjects - misses + x300 * 5), (double) best / allObjects / 6);
	}

	private static Pair<Integer, Integer> getBest300s(int allObjects, int misses, double acc) {
		/*
		 * Why does this work?
		 *
		 * _50s are uniquely determined by _300s and _50s.
		 * => _50s becomes slack variable.
		 * => Solution polytope is _300s + _100s <= allObjects - misses.
		 * (Tip: draw this. It's 2D)
		 * Figure out analytical, continuous solution.
		 * => Discrete solution must be within +/- _300s, +/- 5 * _100s bounding box.
		 * Make sure that 0 <= _300s <= allObjects - misses.
		 *
		 * Search for _100s can be optimized as well if need be.
		 */
		int guess = (int) Math.round((misses + 6 * allObjects * acc - allObjects) / 5);
		int best = 0;
		int best100s = 0;
		double bestError = Double.POSITIVE_INFINITY;
		for (int i = Math.max(0, guess - 1); i <= Math.min(allObjects - misses, guess + 1); i++) {
			Pair<Integer, Double> inner = getBest100s(allObjects, i, misses, acc);
			double error = Math.abs(acc - inner.getRight());
			if (error < bestError) {
				best = i;
				bestError = error;
				best100s = inner.getLeft();
			}
		}
		return Pair.of(best, best100s);
	}
}
