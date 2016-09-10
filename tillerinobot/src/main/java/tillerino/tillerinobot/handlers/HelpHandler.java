package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;

public class HelpHandler implements CommandHandler {

	@Override
	public Response handle(String command, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException,
			InterruptedException {
		if (getLevenshteinDistance(command.toLowerCase(), "help") <= 1) {
			return new Success(userData.getLanguage().help());
		} else if (getLevenshteinDistance(command.toLowerCase(), "faq") <= 1) {
			return new Success(userData.getLanguage().faq());
		}
		return null;
	}

}
