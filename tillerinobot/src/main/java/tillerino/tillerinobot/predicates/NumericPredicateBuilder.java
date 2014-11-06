package tillerino.tillerinobot.predicates;

import lombok.Value;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.PredicateParser.PredicateBuilder;

/**
 * The correct name of this class would have been
 * <code>NumericBeatmapPropertyRecommendationPredicateBuilder</code>.
 * 
 * @author Tillerino
 *
 * @param <T>
 */
@Value
public class NumericPredicateBuilder<T extends NumericBeatmapProperty>
		implements PredicateBuilder<NumericPropertyPredicate<T>> {
	T property;

	enum Relation {
		EQ, GEQ, LEQ
	}

	@Override
	public NumericPropertyPredicate<T> build(final String argument,
			Language lang) throws UserException {
		if (!argument.toLowerCase()
				.startsWith(property.getName().toLowerCase())) {
			return null;
		}

		String rest = argument.substring(property.getName().length());

		Relation relation = null;

		if (rest.startsWith("<=")) {
			relation = Relation.LEQ;
			rest = rest.substring(2);
		}

		if (rest.startsWith(">=")) {
			relation = Relation.GEQ;
			rest = rest.substring(2);
		}

		if (rest.startsWith("=")) {
			relation = Relation.EQ;
			rest = rest.substring(1);
		}

		if (relation == null) {
			throw new UserException(lang.invalidChoice(argument,
					correctFormat()));
		}

		double d;
		try {
			d = Double.parseDouble(rest);
		} catch (NumberFormatException e) {
			throw new UserException(lang.invalidChoice(argument,
					correctFormat()));
		}

		double max = relation == Relation.EQ || relation == Relation.LEQ ? d
				: Double.POSITIVE_INFINITY;
		double min = relation == Relation.EQ || relation == Relation.GEQ ? d
				: Double.NEGATIVE_INFINITY;

		return new NumericPropertyPredicate<T>(argument, property, min, max);
	}

	public String correctFormat() {
		return property.getName() + ">X | " + property.getName() + "<X | "
				+ property.getName() + "=X where X is a number";
	}
}
