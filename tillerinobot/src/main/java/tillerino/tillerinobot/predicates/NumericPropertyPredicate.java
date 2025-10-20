package tillerino.tillerinobot.predicates;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import lombok.Value;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

@Value
public class NumericPropertyPredicate<T extends NumericBeatmapProperty> implements RecommendationPredicate {
    String originalArgument;
    T property;
    double min;
    boolean includeMin;
    double max;
    boolean includeMax;

    @Override
    public boolean test(BareRecommendation r, OsuApiBeatmap beatmap) {
        double value = property.getValue(beatmap, r.mods());

        if (value < min) {
            return false;
        }
        if (value <= min && !includeMin) {
            return false;
        }
        if (value > max) {
            return false;
        }
        return value < max || includeMax;
    }

    @Override
    @SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
    public boolean contradicts(RecommendationPredicate otherPredicate) {
        if (otherPredicate instanceof NumericPropertyPredicate<?> predicate
                && predicate.property.getClass() == property.getClass()) {
            if (predicate.min > max || min > predicate.max) {
                return true;
            }
            if (predicate.min >= max && predicate.includeMin != includeMax) {
                return true;
            }
            if (min >= predicate.max && includeMin != predicate.includeMax) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<String> findNonPredicateContradiction(RecommendationRequest request) {
        return property.findNonPredicateContradiction(request, this);
    }
}
