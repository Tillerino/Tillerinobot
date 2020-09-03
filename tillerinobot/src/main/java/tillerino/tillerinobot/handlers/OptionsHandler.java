package tillerino.tillerinobot.handlers;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OptionsHandler implements CommandHandler {
	final RecommendationRequestParser requestParser;

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser,
			UserData userData) throws UserException,
			IOException, SQLException {
		boolean set = false;
		
		if (command.toLowerCase().startsWith("set")) {
			set = true;
			command = command.substring("set".length()).trim();
		} else if (command.toLowerCase().startsWith("show")
				|| command.toLowerCase().startsWith("view")) {
			command = command.substring("show".length()).trim();
		} else if (command.toLowerCase().startsWith("get")) {
			command = command.substring("get".length()).trim();
		} else {
			return null;
		}

		String option = command.toLowerCase();
		String value = null;
		if (set) {
			if (command.contains(" ")) {
				option = command.substring(0, command.indexOf(' ')).toLowerCase();
				value = command.substring(option.length() + 1);
			} else {
				value = "";
			}
		}

		if (option.equals("lang") || getLevenshteinDistance(option, "language") <= 1) {
			if (set) {
				LanguageIdentifier ident;
				try {
					ident = find(LanguageIdentifier.values(), i -> i.token, value);
				} catch (IllegalArgumentException e) {
					String choices = Stream.of(LanguageIdentifier.values())
							.map(i -> i.token)
							.sorted()
							.collect(joining(", "));
					throw new UserException(userData.getLanguage().invalidChoice(value, choices));
				}

				userData.setLanguage(ident);

				return userData.getLanguage().optionalCommentOnLanguage(apiUser);
			} else {
				return new Message("Language: " + userData.getLanguageIdentifier().token);
			}
		} else if (getLevenshteinDistance(option, "welcome") <= 1 && userData.getHearts() > 0) {
			if (set) {
				userData.setShowWelcomeMessage(parseBoolean(value, userData.getLanguage()));
			} else {
				return new Message("Welcome Message: " + (userData.isShowWelcomeMessage() ? "ON" : "OFF"));
			}
		} else if (getLevenshteinDistance(option, "osutrack-welcome") <= 1 && userData.getHearts() > 0) {
			if (set) {
				userData.setOsuTrackWelcomeEnabled(parseBoolean(value, userData.getLanguage()));
			} else {
				return new Message("osu!track on welcome: " + (userData.isOsuTrackWelcomeEnabled() ? "ON" : "OFF"));
			}
		} else if (getLevenshteinDistance(option, "default") <= 1) {
			if (set) {
				if (value.isEmpty()) {
					userData.setDefaultRecommendationOptions(null);
				} else {
					requestParser.parseSamplerSettings(apiUser, value, userData.getLanguage());
					userData.setDefaultRecommendationOptions(value);
				}
			} else {
				return new Message(
						"Default recommendation settings: " + (userData.getDefaultRecommendationOptions() != null
								? userData.getDefaultRecommendationOptions() : "-"));
			}
		} else {
			throw new UserException(userData.getLanguage().invalidChoice(option,
					"Language, Default" + (userData.getHearts() > 0 ? ", Welcome" : "")));
		}

		return GameChatResponse.none();
	}

	public static boolean parseBoolean(final @Nonnull String original, Language lang) throws UserException {
		String s = original.toLowerCase();
		if(s.equals("on") || s.equals("true") || s.equals("yes") || s.equals("1")) {
			return true;
		}
		if(s.equals("off") || s.equals("false") || s.equals("no") || s.equals("0")) {
			return false;
		}
		throw new UserException(lang.invalidChoice(original, "on|true|yes|1|off|false|no|0"));
	}
	
	public static @Nonnull <E extends Enum<E>> E find(@Nonnull E[] haystack, Function<E, String> token, @Nonnull String needle) {
		needle = needle.toLowerCase();
		
		List<E> found = new ArrayList<>();
		int bestDistance = Integer.MAX_VALUE;
		
		for (int i = 0; i < haystack.length; i++) {
			int distance = getLevenshteinDistance(token.apply(haystack[i]).toLowerCase(), needle);
			if (distance > 1) {
				continue;
			}
			if(distance < bestDistance) {
				bestDistance = distance;
				found.clear();
				found.add(haystack[i]);
			} else if (distance == bestDistance) {
				found.add(haystack[i]);
			}
		}
		
		if(found.isEmpty()) {
			throw new IllegalArgumentException(String.format("nothing matches %s", needle));
		}

		if(found.size() > 1) {
			throw new IllegalArgumentException(String.format("%s all match %s", found, needle));
		}

		return found.get(0);
	}
}
