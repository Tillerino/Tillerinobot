package tillerino.tillerinobot.diff;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.OsuApiScore;

import tillerino.tillerinobot.UserException;

public class AccuracyDistributionTest {
	@Test
	public void testOptimization() throws Exception {
		// takes one second.
		// the randoms and the first getaccuracy takes about .66 seconds.
		// => get accuracy distribution takes about 16 ns
		for(int i = 0; i < 20000000; i++) {
			int _300s = (int) (Math.random() * 100);
			int _100s = (int) (Math.random() * 100);
			int _50s = (int) (Math.random() * 100);
			int misses = (int) (Math.random() * 100);
			
			double acc = OsuApiScore.getAccuracy(_300s, _100s, _50s, misses);
			
			AccuracyDistribution accuracyDistribution = AccuracyDistribution.closest(
					_300s + _100s + _50s + misses, misses,
					acc);
			
			double rec = OsuApiScore.getAccuracy(accuracyDistribution.getX300(),
					accuracyDistribution.getX100(), accuracyDistribution.getX50(),
					accuracyDistribution.getMiss());
			
			assertEquals(acc, rec, 0d);
		}
	}

	@Test
	public void testModelLineNoMisses() throws Exception {
		for (double acc = 1; acc > 0.16; acc -= 0.01) {
			AccuracyDistribution model = AccuracyDistribution.model(10000, 0, acc);
			double rec = OsuApiScore.getAccuracy(model.getX300(),
					model.getX100(), model.getX50(),
					model.getMiss());
			assertEquals(acc, rec, 0.0001);
		}
	}

	@Test
	public void testModelLineHalfMisses() throws Exception {
		for (double acc = .5; acc > 0.16; acc -= 0.01) {
			AccuracyDistribution model = AccuracyDistribution.model(10000, 5000, acc);
			double rec = OsuApiScore.getAccuracy(model.getX300(),
					model.getX100(), model.getX50(),
					model.getMiss());
			assertEquals(acc, rec, 0.0001);
		}
	}

	@Test
	public void testModel() throws Exception {
		for(int i = 0; i < 20000000; i++) {
			int all = 500 + (int) (Math.random() * 500);
			double a = Math.random();
			double b = Math.random();
			double c = Math.random();
			double d = Math.random();
			double x = a + b + c + d;
			int _300s = (int) (all * a / x);
			int _100s = (int) (all * b / x);
			int _50s = (int) (all * c / x);
			int misses = (int) (all * d / x);

			double acc = OsuApiScore.getAccuracy(_300s, _100s, _50s, misses);

			AccuracyDistribution model = AccuracyDistribution.model(
					_300s + _100s + _50s + misses, misses, acc);

			double rec = OsuApiScore.getAccuracy(model.getX300(),
					model.getX100(), model.getX50(),
					model.getMiss());

			// The accuracy is down to swapping a single 300 for a 100 and vice versa.
			// With 500 objects, our accuracy is +- 1/500 * 1/3 ~ 0.00066666...
			double precision = 0.0007;

			if (Math.abs(acc - rec) > precision) {
				System.out.printf("%s %s, %s, %s, %s | %s, %s, %s, %s | %s %s%n",
						rec > acc ? "UP" : "DOWN",_300s, _100s, _50s, misses,
						model.getX300(), model.getX100(), model.getX50(), model.getMiss(),
						acc, rec);
			}
			assertEquals(acc, rec, precision);
		}
	}

	@Test
	public void testIllegalAccuracy() throws Exception {
		assertThatThrownBy(() -> AccuracyDistribution.model(100, 50, .6))
			.isInstanceOf(UserException.class)
			.hasMessageContaining("accuracy must be between 8.3");
	}
}
