package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class ResetHandler implements CommandHandler {
	RecommendationsManager backend;

	@Inject
	public ResetHandler(RecommendationsManager backend) {
		super();
		this.backend = backend;
	}

	@Override
	public Response handle(String command, OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException {
		if (!command.equalsIgnoreCase("reset"))
			return null;

		backend.forgetRecommendations(apiUser.getUserId());

		return new NoResponse();
	}
}