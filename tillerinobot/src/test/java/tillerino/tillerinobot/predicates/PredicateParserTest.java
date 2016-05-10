package tillerino.tillerinobot.predicates;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tillerino.osuApiModel.Mods;


public class PredicateParserTest {
	PredicateParser parser = new PredicateParser();

	@Test
	public void testApproachRate() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("AR=9", null);

		assertEquals(new NumericPropertyPredicate<>(
				"AR=9", new ApproachRate(), 9, true, 9, true), predicate);
	}

	@Test
	public void testOverallDifficulty() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("OD=9", null);

		assertEquals(new NumericPropertyPredicate<>(
				"OD=9", new OverallDifficulty(), 9, true, 9, true), predicate);
	}

	@Test
	public void testBPM() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("BPM>=9000", null);

		assertEquals(new NumericPropertyPredicate<>(
				"BPM>=9000", new BeatsPerMinute(), 9000, true,
				Double.POSITIVE_INFINITY, true),
				predicate);
	}

	@Test
	public void testExcludeMods() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("-hr", null);

		assertEquals(new ExcludeMod(Mods.HardRock),
				predicate);
	}

	@Test
	public void testUnknown() throws Exception {
		assertNull(parser.tryParse("yourMom", null));
	}
}
