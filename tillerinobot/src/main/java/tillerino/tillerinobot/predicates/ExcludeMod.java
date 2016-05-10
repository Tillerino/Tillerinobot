package tillerino.tillerinobot.predicates;

import lombok.Value;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.PredicateParser.PredicateBuilder;

@Value
public class ExcludeMod implements RecommendationPredicate {
	Mods mod;

	@Override
	public boolean test(BareRecommendation r, OsuApiBeatmap beatmap) {
		return !mod.is(r.getMods());
	}

	@Override
	public boolean contradicts(RecommendationPredicate otherPredicate) {
		return false;
	}

	@Override
	public String getOriginalArgument() {
		return "-" + mod.getShortName();
	}

	public static class Builder implements PredicateBuilder<ExcludeMod> {
		@Override
		public ExcludeMod build(String argument, Language lang) throws UserException {
			if (!argument.startsWith("-")) {
				return null;
			}
			try {
				return new ExcludeMod(Mods.fromShortName(argument.substring(1).toUpperCase()));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

	}
}
