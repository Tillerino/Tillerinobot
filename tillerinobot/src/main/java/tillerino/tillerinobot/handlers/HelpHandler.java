package tillerino.tillerinobot.handlers;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

public class HelpHandler implements CommandHandler {

    @Override
    public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language lang)
            throws UserException, IOException, SQLException, InterruptedException {
        if (getLevenshteinDistance(command.toLowerCase(), "help") <= 1) {
            return new Success(lang.help());
        } else if (getLevenshteinDistance(command.toLowerCase(), "faq") <= 1) {
            return new Success(lang.faq());
        }
        return null;
    }
}
