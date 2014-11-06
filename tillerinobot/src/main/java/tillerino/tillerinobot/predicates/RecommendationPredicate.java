package tillerino.tillerinobot.predicates;

import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;

public interface RecommendationPredicate {
	boolean test(BareRecommendation r, OsuApiBeatmap beatmap);

	boolean contradicts(RecommendationPredicate otherPredicate);

	String getOriginalArgument();
}