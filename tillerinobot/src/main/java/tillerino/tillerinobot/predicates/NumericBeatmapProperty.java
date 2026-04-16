package tillerino.tillerinobot.predicates;

import java.util.Optional;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

public interface NumericBeatmapProperty {
    String getName();

    double getValue(ApiBeatmap beatmap, @BitwiseMods long mods);

    /**
     * see {@link RecommendationPredicate#findNonPredicateContradiction(RecommendationRequest)}
     *
     * @param value the parsed value for this property
     */
    default Optional<String> findNonPredicateContradiction(
            RecommendationRequest request, NumericPropertyPredicate<?> value) {
        return Optional.empty();
    }
}
