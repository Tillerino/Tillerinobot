package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager;
import tillerino.tillerinobot.RecommendationsManager.Sampler.Settings;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.LanguageIdentifier;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class OptionsHandler implements CommandHandler {
	@Override
	public boolean handle(String command, IRCBotUser ircUser,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException {
		boolean set = false;
		
		if (command.toLowerCase().startsWith("set")) {
			set = true;
			command = command.substring("set".length()).trim();
		} else if (command.toLowerCase().startsWith("show")
				|| command.toLowerCase().startsWith("view")) {
			command = command.substring("show".length()).trim();
		} else {
			return false;
		}
		
		if (set && !command.contains(" ")) {
			throw new UserException(userData.getLanguage().setFormat());
		}

		String option = set ? command.substring(0, command.indexOf(' '))
				.toLowerCase() : command.toLowerCase();
		String value = set ? command.substring(option.length() + 1) : null;

		if (option.equals("lang") || getLevenshteinDistance(option, "language") <= 1) {
			if (set) {
				LanguageIdentifier ident;
				try {
					ident = find(LanguageIdentifier.values(), value);
				} catch (IllegalArgumentException e) {
					LanguageIdentifier[] values = LanguageIdentifier.values();
					Arrays.sort(values, new Comparator<LanguageIdentifier>() {
						@Override
						public int compare(LanguageIdentifier o1, LanguageIdentifier o2) {
							return o1.toString().compareTo(o2.toString());
						}
					});
					throw new UserException(userData.getLanguage().invalidChoice(value,
							join(values, ", ")));
				}

				userData.setLanguage(ident);

				userData.getLanguage().optionalCommentOnLanguage(ircUser,
						apiUser);
			} else {
				ircUser.message("Language: " + userData.getLanguageIdentifier().toString());
			}
		} else if (getLevenshteinDistance(option, "welcome") <= 1 && userData.getHearts() > 0) {
			if (set) {
				userData.setShowWelcomeMessage(parseBoolean(value, userData.getLanguage()));
			} else {
				ircUser.message("Welcome Message: " + (userData.isShowWelcomeMessage() ? "ON" : "OFF"));
			}
		} else if (getLevenshteinDistance(option, "rdefaults") <= 1) {
			if (set) {
				userData.setRecommendMods(parseStr(apiUser, value, userData.getLanguage()));
			} else {
				ircUser.message("Recommendation defaults: " + userData.getRecommendationDefault());
			}
	    } else {
			throw new UserException(userData.getLanguage().invalidChoice(option,
					"Language" + (userData.getHearts() > 0 ? ", Welcome" : "")));
		}

		return true;
	}

	private static boolean parseBoolean(final @Nonnull String original, Language lang) throws UserException {
		String s = original.toLowerCase();
		if(s.equals("on") || s.equals("true") || s.equals("yes") || s.equals("1")) {
			return true;
		}
		if(s.equals("off") || s.equals("false") || s.equals("no") || s.equals("0")) {
			return false;
		}
		throw new UserException(lang.invalidChoice(original, "on|true|yes|1|off|false|no|0"));
	}
	
	public static String parseStr(OsuApiUser apiUser, String value, Language lang) throws UserException, SQLException, IOException {

		Settings set = RecommendationsManager.parseSamplerSettings(null, null, apiUser, value, lang);
		String model = set.model.toString().substring(0, 1).toUpperCase() 
			     + set.model.toString().toLowerCase().substring(1);
		String[] mods = new String[]{};
		
		if(set.requestedMods != 0)
		{
			mods = Mods.getMods(set.requestedMods).toString().replaceAll("[\\[\\]]", "").split(" ");
		}
		else if(set.nomod)
		{
			mods = new String[]{"Nomod"};
		}
		else
		{
			mods = new String[]{"Any"};
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(model + " | ");
		int i = 1;
		for (String mod : mods)
		{
		    sb.append(mod);
		    if(i == mods.length)
		    {
		    	sb.append(".");
		    }
		    else
		    {
		        sb.append(" ");
		    }
		    i++;
		}
		
		return sb.toString();
	}
	
	public static @Nonnull <E extends Enum<E>> E find(@Nonnull E[] haystack, @Nonnull String needle) {
		needle = needle.toLowerCase();
		
		E found = null;
		
		for (int i = 0; i < haystack.length; i++) {
			if(getLevenshteinDistance(haystack[i].toString().toLowerCase(), needle) <= 1) {
				if(found != null) {
					throw new IllegalArgumentException();
				}
				found = haystack[i];
			}
		}
		
		if(found == null) {
			throw new IllegalArgumentException();
		}
		
		return found;
	}
}
