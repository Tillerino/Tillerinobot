package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;

@Value
@AllArgsConstructor(onConstructor=@__({@Inject}))
public class LinkPpaddictHandler implements CommandHandler {
	public static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-z]{32}");
	
	BotBackend backend;
	
	@Override
	public boolean handle(String command, IRCBotUser ircUser, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException {
		if(!TOKEN_PATTERN.matcher(command).matches()) {
			return false;
		}
		String ppaddictName = backend.tryLinkToPpaddict(command, apiUser);
		if(ppaddictName == null)
			ircUser.message("nothing happened.");
		else
			ircUser.message("linked to " + ppaddictName);
		return true;
	}

}
