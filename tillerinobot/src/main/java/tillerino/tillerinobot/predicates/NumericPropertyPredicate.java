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
	boolean includeMin;
	double max;
	boolean includeMax;

	@Override
	public boolean test(BareRecommendation r, OsuApiBeatmap beatmap) {
		double value = property.getValue(beatmap, r.getMods());

		if(value < min) {
			return false;
		}
		if(value <= min && !includeMin) {
			return false;
		}
		if(value > max) {
			return false;
		}
		return value < max || includeMax;
	}

	@Override
	public boolean contradicts(RecommendationPredicate otherPredicate) {
		if (otherPredicate instanceof NumericPropertyPredicate<?>) {
			NumericPropertyPredicate<?> predicate = (NumericPropertyPredicate<?>) otherPredicate;

			if (predicate.property.getClass() == property.getClass()) {
				if (predicate.min > max || min > predicate.max) {
					return true;
				}
				if(predicate.min >= max && predicate.includeMin != includeMax) {
					return true;
				}
				if(min >= predicate.max && includeMin != predicate.includeMax) {
					return true;
				}
			}
		}

		return false;
	}

}
