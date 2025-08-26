package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.handlers.options.DefaultOptionHandler;
import tillerino.tillerinobot.handlers.options.LangOptionHandler;
import tillerino.tillerinobot.handlers.options.MapMetaDataOptionHandler;
import tillerino.tillerinobot.handlers.options.OptionHandler;
import tillerino.tillerinobot.handlers.options.OsutrackWelcomeOptionHandler;
import tillerino.tillerinobot.handlers.options.V2ApiOptionHandler;
import tillerino.tillerinobot.handlers.options.WelcomeOptionHandler;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;

public class OptionsHandler implements CommandHandler {
	private final List<OptionHandler> optionHandlers = new ArrayList<>();

	@Inject
	public OptionsHandler(RecommendationRequestParser requestParser) {
		optionHandlers.add(new LangOptionHandler());
		optionHandlers.add(new WelcomeOptionHandler());
		optionHandlers.add(new OsutrackWelcomeOptionHandler());
		optionHandlers.add(new DefaultOptionHandler(requestParser));
		optionHandlers.add(new MapMetaDataOptionHandler());
		optionHandlers.add(new V2ApiOptionHandler());
	}

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser,
			UserData userData, Language lang) throws UserException,
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

		for (OptionHandler optionHandler : optionHandlers) {
			GameChatResponse resposne = optionHandler.handle(option, set, value, userData, apiUser, lang);
			if(resposne != null) return resposne;
		}

		int userHearts = userData.getHearts();
		String validOptions = optionHandlers.stream()
				.filter(x -> userHearts >= x.getMinHearts())
				.map(OptionHandler::getOptionName)
				.collect(Collectors.joining(", "));
		throw new UserException(lang.invalidChoice(option, validOptions));
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
