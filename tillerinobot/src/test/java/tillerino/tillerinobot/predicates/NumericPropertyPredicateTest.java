package tillerino.tillerinobot.predicates;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.recommendations.BareRecommendation;

public class NumericPropertyPredicateTest {
	@Test
	public void testOkay() throws Exception {
		RecommendationPredicate predicate = new NumericPropertyPredicate<>(
				"w/e", new TitleLength(), 3, true, 3, true);

		OsuApiBeatmap okayBeatmap = mock(OsuApiBeatmap.class);
		when(okayBeatmap.getTitle()).thenReturn("hai");
		BareRecommendation rec = mock(BareRecommendation.class);

		assertTrue(predicate.test(rec, okayBeatmap));

		OsuApiBeatmap tooLongBeatmap = mock(OsuApiBeatmap.class);
		when(tooLongBeatmap.getTitle()).thenReturn("weeeeeeeeee");

		assertFalse(predicate.test(rec, tooLongBeatmap));

		OsuApiBeatmap tooShortBeatmap = mock(OsuApiBeatmap.class);
		when(tooShortBeatmap.getTitle()).thenReturn("m");

		assertFalse(predicate.test(rec, tooShortBeatmap));

		RecommendationPredicate predicateExcl = new NumericPropertyPredicate<>(
				"w/e", new TitleLength(), 3, false, 11, false);
		
		assertFalse(predicateExcl.test(rec, okayBeatmap));
		
		assertFalse(predicateExcl.test(rec, tooLongBeatmap));

		RecommendationPredicate predicateHuge = new NumericPropertyPredicate<>(
				"w/e", new TitleLength(), 1, false, 15, false);
		
		assertTrue(predicateHuge.test(rec, okayBeatmap));
}

	@Test
	public void testMods() throws Exception {
		RecommendationPredicate predicate = new NumericPropertyPredicate<>(
				"w/e", new TitleLength(), 2, true, 5, true);

		OsuApiBeatmap okayBeatmap = mock(OsuApiBeatmap.class);
		when(okayBeatmap.getTitle()).thenReturn("hai");
		BareRecommendation okayRec = mock(BareRecommendation.class);

		assertTrue(predicate.test(okayRec, okayBeatmap));

		BareRecommendation moddedRec = mock(BareRecommendation.class);
		when(moddedRec.getMods()).thenReturn(1l);

		assertFalse(predicate.test(moddedRec, okayBeatmap));
	}

	@Test
	public void testContradiction() throws Exception {
		RecommendationPredicate predicate1 = new NumericPropertyPredicate<>("w/e", new TitleLength(), 2, true, 5, true);

		RecommendationPredicate predicate2 = new NumericPropertyPredicate<>("w/e", new TitleLength(), 6, true, 7, true);

		RecommendationPredicate predicate3 = new NumericPropertyPredicate<>("w/e", new TitleLength(), 7, true, 9, true);
		RecommendationPredicate predicate4 = new NumericPropertyPredicate<>("w/e", new TitleLength(), 7, false, 9, true);

		assertTrue(predicate1.contradicts(predicate2));
		assertTrue(predicate2.contradicts(predicate1));

		assertFalse(predicate1.contradicts(predicate1));
		assertFalse(predicate2.contradicts(predicate2));

		assertFalse(predicate2.contradicts(predicate3));
		assertFalse(predicate3.contradicts(predicate2));

		assertTrue(predicate2.contradicts(predicate4));
		assertTrue(predicate4.contradicts(predicate2));
	}
}
