package tillerino.tillerinobot.predicates;

import java.util.Optional;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BitwiseMods;

import tillerino.tillerinobot.recommendations.RecommendationRequest;

public interface NumericBeatmapProperty {
	String getName();

	double getValue(OsuApiBeatmap beatmap, @BitwiseMods long mods);

	/**
	 * see
	 * {@link RecommendationPredicate#findNonPredicateContradiction(RecommendationRequest)}
	 * 
	 * @param value the parsed value for this property
	 */
	default Optional<String> findNonPredicateContradiction(RecommendationRequest request, NumericPropertyPredicate<?> value) {
		return Optional.empty();
	}
}