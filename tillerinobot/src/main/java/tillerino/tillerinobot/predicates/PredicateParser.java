package tillerino.tillerinobot.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;

import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class PredicateParser {
	public interface PredicateBuilder<T extends RecommendationPredicate> {
		T build(String argument, Language lang) throws UserException;
	}

	List<PredicateBuilder<?>> builders = new ArrayList<>();

	public PredicateParser() {
		List<NumericBeatmapProperty> properties = Arrays.asList(
				new ApproachRate(), new BeatsPerMinute(), new OverallDifficulty(), new MapLength());

		for (NumericBeatmapProperty property : properties) {
			builders.add(new NumericPredicateBuilder<>(property));
		}
	}

	public @CheckForNull RecommendationPredicate tryParse(String argument,
			Language lang) throws UserException {
		for (PredicateBuilder<?> predicateBuilder : builders) {
			RecommendationPredicate predicate = predicateBuilder.build(
					argument, lang);
			if (predicate != null)
				return predicate;
		}
		return null;
	}
}
