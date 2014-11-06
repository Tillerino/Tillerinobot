package tillerino.tillerinobot.predicates;

import static org.junit.Assert.*;

import org.junit.Test;


public class PredicateParserTest {
	PredicateParser parser = new PredicateParser();

	@Test
	public void testApproachRate() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("AR=9", null);

		assertEquals(new NumericPropertyPredicate<>(
				"AR=9", new ApproachRate(), 9, 9), predicate);
	}

	@Test
	public void testBPM() throws Exception {
		RecommendationPredicate predicate = parser.tryParse("BPM>=9000", null);

		assertEquals(new NumericPropertyPredicate<>(
				"BPM>=9000", new BeatsPerMinute(), 9000,
				Double.POSITIVE_INFINITY),
				predicate);
	}

	@Test
	public void testUnknown() throws Exception {
		assertNull(parser.tryParse("yourMom", null));
	}
}
