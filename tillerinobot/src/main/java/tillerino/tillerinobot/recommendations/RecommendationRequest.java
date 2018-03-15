package tillerino.tillerinobot.recommendations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.Builder;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.predicates.RecommendationPredicate;

@Value
@Builder
public class RecommendationRequest {
	public RecommendationRequest(boolean nomod, Model model, @BitwiseMods long requestedMods,
			List<RecommendationPredicate> predicates) {
		super();
		this.nomod = nomod;
		this.model = model;
		this.requestedMods = requestedMods;
		this.predicates = predicates;
	}

	public static class RecommendationRequestBuilder {
		@Getter(onMethod = @__(@BitwiseMods))
		@BitwiseMods
		private long requestedMods = 0L;

		List<RecommendationPredicate> predicates = new ArrayList<>();

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

	boolean nomod;
	Model model;
	@Getter(onMethod = @__(@BitwiseMods))
	@BitwiseMods
	long requestedMods;
	List<RecommendationPredicate> predicates;
}
