package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.UserId;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class FixIDHandler implements CommandHandler {
	private static final String COMMAND = "!fixid";
	BotBackend backend;

	@Inject
	public FixIDHandler(BotBackend backend) {
		super();
		this.backend = backend;
	}

	@Override
	public boolean handle(String command, IRCBotUser ircUser, OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException {

        if (!command.toLowerCase().startsWith(COMMAND)) {
            return false;
        }

        String idStr = command.substring(COMMAND.length()).trim();
		int id = parseId(idStr);

		OsuApiUser user = backend.resolveManually(id);
		if(user == null) {
			ircUser.message("That user-id does not exist :(");
		} else {
            ircUser.message("User '" + user.getUserName() + "' is now resolvable to user-id " + user.getUserId());
		}

		return true;
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