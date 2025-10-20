package tillerino.tillerinobot.predicates;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

@EqualsAndHashCode
public class StarDiff implements NumericBeatmapProperty {
    @Override
    public String getName() {
        return "STAR";
    }

    @Override
    public double getValue(OsuApiBeatmap beatmap, long mods) {
        return beatmap.getStarDifficulty();
    }

    @Override
    public Optional<String> findNonPredicateContradiction(
            RecommendationRequest request, NumericPropertyPredicate<?> value) {
        if (request.requestedMods() != 0L) {
            return Optional.of(String.format(
                    "%s %s",
                    Mods.toShortNamesContinuous(Mods.getMods(request.requestedMods())), value.getOriginalArgument()));
        }
        return Optional.empty();
    }
}
