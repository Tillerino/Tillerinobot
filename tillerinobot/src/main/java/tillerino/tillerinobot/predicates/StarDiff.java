package tillerino.tillerinobot.predicates;

import java.util.Optional;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import lombok.EqualsAndHashCode;
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
    public Optional<String> findNonPredicateContradiction(RecommendationRequest request, NumericPropertyPredicate<?> value) {
        if (request.getRequestedMods() != 0L) {
            return Optional.of(String.format("%s %s", Mods.toShortNamesContinuous(Mods.getMods(request.getRequestedMods())), value.getOriginalArgument()));
        }
        return Optional.empty();
    }
}
