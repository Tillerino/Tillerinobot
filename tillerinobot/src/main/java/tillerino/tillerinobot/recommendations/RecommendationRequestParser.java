package tillerino.tillerinobot.recommendations;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.PredicateParser;
import tillerino.tillerinobot.predicates.RecommendationPredicate;
import tillerino.tillerinobot.recommendations.RecommendationRequest.RecommendationRequestBuilder;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecommendationRequestParser {
	private static final String STANDARD_SYNTAX = "[nomod] [relax|beta|gamma] [dt] [hr] [hd]";

	private final BotBackend backend;
	
	PredicateParser parser = new PredicateParser();

	/**
	 * Parses a recommendation request string.
	 *
	 * @param message
	 *          the string after the trigger word, e.g. "!r " has been removed.
	 * @return a validated request
	 */
	public RecommendationRequest parseSamplerSettings(OsuApiUser apiUser, @Nonnull String message,
			Language lang) throws UserException, SQLException, IOException {
		String[] remaining = message.split(" ");
		
		RecommendationRequestBuilder settingsBuilder = RecommendationRequest.builder();
		
		settingsBuilder.model(Model.GAMMA);

		for (int i = 0; i < remaining.length; i++) {
			String param = remaining[i];
			if(param.length() == 0)
				continue;
			if (!parseEngines(param, settingsBuilder)
					&& !parseMods(param, settingsBuilder)
					&& !parsePredicates(param, settingsBuilder, apiUser, lang)) {
				throw new UserException(lang.invalidChoice(param, STANDARD_SYNTAX));
			}
		}

		RecommendationRequest request = settingsBuilder.build();
		/*
		 * verify the arguments
		 */
		
		if(request.isNomod() && request.getRequestedMods() != 0) {
			throw new UserException(lang.mixedNomodAndMods());
		}

		for (RecommendationPredicate predicate : request.getPredicates()) {
			Optional<String> contradiction = predicate.findNonPredicateContradiction(request);
			if (contradiction.isPresent()) {
				throw new UserException(lang.invalidChoice(contradiction.get(), STANDARD_SYNTAX));
			}
		}

		return request;
	}

	private boolean parseEngines(String param, RecommendationRequestBuilder settingsBuilder) {
		String lowerCase = param.toLowerCase();
		if(getLevenshteinDistance(lowerCase, "relax") <= 2) {
			settingsBuilder.model(Model.ALPHA);
			return true;
		}
		if(getLevenshteinDistance(lowerCase, "beta") <= 1) {
			settingsBuilder.model(Model.BETA);
			return true;
		}
		if(getLevenshteinDistance(lowerCase, "gamma") <= 2) {
			settingsBuilder.model(Model.GAMMA);
			return true;
		}
		return false;
	}

	private boolean parseMods(String param, RecommendationRequestBuilder settingsBuilder) {
		String lowerCase = param.toLowerCase();
		if(getLevenshteinDistance(lowerCase, "nomod") <= 2) {
			settingsBuilder.nomod(true);
			return true;
		}
		if (settingsBuilder.getModel() == Model.GAMMA) {
			Long mods = Mods.fromShortNamesContinuous(lowerCase);
			if (mods != null) {
				mods = Mods.fixNC(mods);
				if (mods == (mods & Mods.getMask(Mods.DoubleTime, Mods.HardRock, Mods.Hidden))) {
					for (Mods mod : Mods.getMods(mods)) {
						settingsBuilder.requestedMods(Mods.add(settingsBuilder.getRequestedMods(), mod));
					}
					return true;
				}
			}
		}
		return false;
	}

	private boolean parsePredicates(String param, RecommendationRequestBuilder settingsBuilder, OsuApiUser apiUser,
			Language lang) throws SQLException, IOException, UserException {
		if (backend.getDonator(apiUser) > 0) {
			RecommendationPredicate predicate = parser.tryParse(param, lang);
			if (predicate != null) {
				for (RecommendationPredicate existingPredicate : settingsBuilder.getPredicates()) {
					if (existingPredicate.contradicts(predicate)) {
						throw new UserException(lang.invalidChoice(
								existingPredicate.getOriginalArgument() + " with "
										+ predicate.getOriginalArgument(),
								"either " + existingPredicate.getOriginalArgument() + " or "
										+ predicate.getOriginalArgument()));
					}
				}
				settingsBuilder.predicate(predicate);
				return true;
			}
		}
		return false;
	}
}
