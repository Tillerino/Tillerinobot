package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.LanguageIdentifier;
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

		if (option.equals("lang") || option.equals("language")) {
			if (set) {
				LanguageIdentifier ident;
				try {
					ident = LanguageIdentifier.valueOf(value);
				} catch (IllegalArgumentException e) {
					throw new UserException(userData.getLanguage().invalidChoice(value,
							StringUtils.join(LanguageIdentifier.values(), ", ")));
				}

				userData.setLanguage(ident);

				userData.getLanguage().optionalCommentOnLanguage(ircUser,
						apiUser);
			} else {
				ircUser.message("Language: " + userData.getLanguageIdentifier().toString());
			}
		} else if (option.equals("welcome") && userData.getHearts() > 0) {
			if (set) {
				userData.setShowWelcomeMessage(parseBoolean(value, userData.getLanguage()));
			} else {
				ircUser.message("Welcome Message: " + (userData.isShowWelcomeMessage() ? "ON" : "OFF"));
			}
		} else {
			throw new UserException(userData.getLanguage().invalidChoice(option,
					"Language" + (userData.getHearts() > 0 ? ", Welcome" : "")));
		}

		return true;
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
}
