package tillerino.tillerinobot.handlers;

import java.sql.SQLException;
import javax.inject.Inject;
import org.tillerino.ppaddict.chat.GameChatResponse;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

public class ResetHandler implements CommandHandler {
    final RecommendationsManager backend;

    @Inject
    public ResetHandler(RecommendationsManager backend) {
        super();
        this.backend = backend;
    }

    @Override
    public GameChatResponse handle(String command, ApiUser apiUser, UserData userData, Language lang)
            throws SQLException {
        if (!command.equalsIgnoreCase("reset")) return null;

        backend.forgetRecommendations(apiUser.getUserId());

        return GameChatResponse.none();
    }
}
