package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import org.tillerino.osuApiModel.OsuApiUser;

import com.google.inject.Inject;

import tillerino.tillerinobot.RecommendationsManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;

public class ResetHandler implements CommandHandler {
	RecommendationsManager backend;

	@Inject
	public ResetHandler(RecommendationsManager backend) {
		super();
		this.backend = backend;
	}

	@Override
	public boolean handle(String command, IRCBotUser ircUser, OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException {
		if (!command.equalsIgnoreCase("reset"))
			return false;

		backend.forgetRecommendations(apiUser.getUserId());

		return true;
	}
}