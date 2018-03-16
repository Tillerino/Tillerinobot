package tillerino.tillerinobot.diff;

import java.util.function.IntToDoubleFunction;

import lombok.Value;

import org.apache.commons.lang3.tuple.Pair;
import org.tillerino.osuApiModel.OsuApiScore;

@Value
public class AccuracyDistribution {
	final int x300;
	final int x100;
	final int x50;
	final int miss;
	
	public static AccuracyDistribution get(int allObjects, int misses, double acc) {
		int best300s = getBest300s(allObjects, misses, acc);
		int best100s = getBest100s(allObjects, best300s, misses, acc).getLeft();
		return new AccuracyDistribution(best300s, best100s, allObjects - best300s - best100s - misses, misses);
	}
	
	public static Pair<Integer, Double> bisect(int min, int max, IntToDoubleFunction fun, double target) {
		double minVal = fun.applyAsDouble(min);
		if(minVal >= target || min == max) {
			return Pair.of(min, minVal);
		}
		double maxVal = fun.applyAsDouble(max);
		if(maxVal <= target) {
			return Pair.of(max, maxVal);
		}
		/*
		 * invariant: minVal < target, maxVal > target, min != max
		 */
		for(;;) {
			if(max == min + 1) {
				if(Math.abs(minVal - target) < Math.abs(maxVal - target)) {
					return Pair.of(min, minVal);
				}
				return Pair.of(max, maxVal);
			}
			int center = (max + min) / 2;
			double centerVal = fun.applyAsDouble(center);
			if(centerVal <= target) {
				min = center;
				minVal = centerVal;
			} else {
				max = center;
				maxVal = centerVal;
			}
		}
	}
	
	public static Pair<Integer, Double> getBest100s(int allObjects, int x300, int misses, double acc) {
		return bisect(
				0,
				allObjects - x300 - misses,
				x -> OsuApiScore.getAccuracy(x300, x, allObjects - x300 - x
						- misses, misses), acc);
	}
	
	public static int getBest300s(int allObjects, int misses, double acc) {
		int best300s = 0;
		Pair<Integer, Double> best = getBest100s(allObjects, 0, misses, acc);
		for(int i = 1; i <= allObjects - misses; i++) {
			Pair<Integer, Double> val = getBest100s(allObjects, i, misses, acc);
			if(Math.abs(val.getRight() - acc) < Math.abs(best.getRight() - acc)) {
				best = val;
				best300s = i;
			}
		}
		return best300s;
	}
}
