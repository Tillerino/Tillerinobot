package tillerino.tillerinobot.predicates;

import java.util.Optional;

import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

public interface RecommendationPredicate {
	boolean test(BareRecommendation r, OsuApiBeatmap beatmap);

	/**
	 * Checks if this predicate contradicts the given predicate.
	 */
	boolean contradicts(RecommendationPredicate otherPredicate);

	/**
	 * Checks if this predicate contradicts any settings in the request beside other
	 * predicates.
	 *
	 * @return If there is a contradiction, a string describing the contradiction,
	 *         an empty optional otherwise.
	 */
	Optional<String> findNonPredicateContradiction(RecommendationRequest request);

	String getOriginalArgument();
}