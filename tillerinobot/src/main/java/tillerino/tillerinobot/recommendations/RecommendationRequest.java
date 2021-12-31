package tillerino.tillerinobot.recommendations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Getter;
import lombok.Builder;
import tillerino.tillerinobot.predicates.RecommendationPredicate;

@Builder
public record RecommendationRequest(
		boolean nomod,
		Model model,
		@BitwiseMods long requestedMods,
		List<RecommendationPredicate> predicates
		) {
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

}
