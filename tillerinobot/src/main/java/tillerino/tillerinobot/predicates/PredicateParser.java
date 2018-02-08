package tillerino.tillerinobot.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;

import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class PredicateParser {
	public interface PredicateBuilder<T extends RecommendationPredicate> {
		/**
		 * Parses the given string.
		 * 
		 * @param argument
		 *            doesn't contain spaces
		 * @param lang
		 *            for error messages
		 * @return null if the argument cannot be parsed
		 */
		T build(String argument, Language lang) throws UserException;
	}

	List<PredicateBuilder<?>> builders = new ArrayList<>();

	public PredicateParser() {
		List<NumericBeatmapProperty> properties = Arrays.asList(new ApproachRate(), new BeatsPerMinute(), new OverallDifficulty(), new MapLength(), new CircleSize(), new StarDiff());

		for (NumericBeatmapProperty property : properties) {
			builders.add(new NumericPredicateBuilder<>(property));
		}

		builders.add(new ExcludeMod.Builder());
	}

	public @CheckForNull RecommendationPredicate tryParse(String argument, Language lang) throws UserException {
		for (PredicateBuilder<?> predicateBuilder : builders) {
			RecommendationPredicate predicate = predicateBuilder.build(argument, lang);
			if (predicate != null)
				return predicate;
		}
		return null;
	}
}
