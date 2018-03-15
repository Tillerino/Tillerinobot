package tillerino.tillerinobot.diff;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiScore;
import tillerino.tillerinobot.diff.AccuracyDistribution;


public class AccuracyDistributionTest {
	@Test
	public void testOptimization() throws Exception {
		for(int i = 0; i < 10000; i++) {
			int _300s = (int) (Math.random() * 100);
			int _100s = (int) (Math.random() * 100);
			int _50s = (int) (Math.random() * 100);
			int misses = (int) (Math.random() * 100);
			
			double acc = OsuApiScore.getAccuracy(_300s, _100s, _50s, misses);
			
			AccuracyDistribution accuracyDistribution = AccuracyDistribution.get(
					_300s + _100s + _50s + misses, misses,
					acc);
			
			double rec = OsuApiScore.getAccuracy(accuracyDistribution.getX300(),
					accuracyDistribution.getX100(), accuracyDistribution.getX50(),
					accuracyDistribution.getMiss());
			
			assertEquals(acc, rec, 0d);
		}
	}
}
