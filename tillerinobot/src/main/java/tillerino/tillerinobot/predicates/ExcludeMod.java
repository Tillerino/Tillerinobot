package tillerino.tillerinobot.predicates;

import java.util.Optional;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.PredicateParser.PredicateBuilder;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

public record ExcludeMod(Mods mod) implements RecommendationPredicate {
    @Override
    public boolean test(BareRecommendation r, OsuApiBeatmap beatmap) {
        return !mod.is(r.mods());
    }

    @Override
    public boolean contradicts(RecommendationPredicate otherPredicate) {
        return false;
    }

    @Override
    public String originalArgument() {
        return "-" + mod.getShortName();
    }

    public static class Builder implements PredicateBuilder<ExcludeMod> {
        @Override
        public ExcludeMod build(String argument, Language lang) {
            if (!argument.startsWith("-")) {
                return null;
            }
            try {
                Mods mod = Mods.fromShortName(argument.substring(1).toUpperCase());
                if (mod == null) {
                    return null;
                }
                return new ExcludeMod(mod);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Override
    public Optional<String> findNonPredicateContradiction(RecommendationRequest request) {
        if (mod.is(request.requestedMods())) {
            return Optional.of(String.format("%s -%s", mod.getShortName(), mod.getShortName()));
        }
        return Optional.empty();
    }
}
