package tillerino.tillerinobot.recommendations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.predicates.RecommendationPredicate;

@Builder
public record RecommendationRequest(
        boolean nomod,
        Model model,
        @BitwiseMods long requestedMods,
        List<RecommendationPredicate> predicates,
        Shift difficultyShift) {
    public RecommendationRequest {
        predicates = new ArrayList<>(predicates);
    }

    public List<RecommendationPredicate> predicates() {
        return Collections.unmodifiableList(predicates);
    }

    public static class RecommendationRequestBuilder {
        @Getter
        @BitwiseMods
        private long requestedMods = 0L;

        private List<RecommendationPredicate> predicates = new ArrayList<>();

        private Shift difficultyShift = Shift.NONE;

        public RecommendationRequestBuilder requestedMods(@BitwiseMods long requestedMods) {
            this.requestedMods = requestedMods;
            return this;
        }

        public RecommendationRequestBuilder predicate(RecommendationPredicate predicate) {
            predicates.add(predicate);
            return this;
        }

        public Model getModel() {
            return model;
        }

        public List<RecommendationPredicate> getPredicates() {
            return Collections.unmodifiableList(predicates);
        }
    }

    /** Modifies the difficulty of recommendations. */
    enum Shift {
        /** Regular strength. */
        NONE,
        /** The player is weak compared to their top scores. Recommendations are easier. */
        SUCC,
        /** Even weaker than {@link #SUCC} */
        SUCCER,
        /** Even weaker than {@link #SUCCER} */
        SUCCERBERG
    }
}
