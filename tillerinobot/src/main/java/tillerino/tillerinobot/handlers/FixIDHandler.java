package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FixIDHandler implements CommandHandler {
	private static final String COMMAND = "!fixid";
    private final IrcNameResolver resolver;

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language lang)
					throws UserException, IOException, SQLException {

        if (!command.toLowerCase().startsWith(COMMAND)) {
            return null;
        }

        String idStr = command.substring(COMMAND.length()).trim();
		int id = parseId(idStr);

		OsuApiUser user = resolver.resolveManually(id);
		if(user == null) {
			throw new UserException("That user-id does not exist :(");
		} else {
			return new Message("User '" + user.getUserName() + "' is now resolvable to user-id " + user.getUserId());
		}
	}

	@SuppressFBWarnings("TQ")
	@UserId
	private int parseId(String idStr) throws UserException {
		try {
			return Integer.parseInt(idStr);
		} catch (NumberFormatException e) {
			throw new UserException("Invalid user-id :(");
		}
	}
}