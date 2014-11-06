package tillerino.tillerinobot.predicates;

import lombok.Value;

import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;

@Value
public class NumericPropertyPredicate<T extends NumericBeatmapProperty>
		implements RecommendationPredicate {
	String originalArgument;
	T property;
	double min;
	double max;

	@Override
	public boolean test(BareRecommendation r, OsuApiBeatmap beatmap) {
		double value = property.getValue(beatmap, r.getMods());

		return value >= min && value <= max;
	}

	@Override
	public boolean contradicts(RecommendationPredicate otherPredicate) {
		if (otherPredicate instanceof NumericPropertyPredicate<?>) {
			NumericPropertyPredicate<?> predicate = (NumericPropertyPredicate<?>) otherPredicate;

			if (predicate.property.getClass() == property.getClass()) {
				if (predicate.min > max || min > predicate.max) {
					return true;
				}
			}
		}

		return false;
	}

}
