package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.Value;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;

@Value
@AllArgsConstructor(onConstructor=@__({@Inject}))
public class LinkPpaddictHandler implements CommandHandler {
	public static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-z]{32}");
	
	BotBackend backend;
	
	@Override
	public Response handle(String command, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException {
		if(!TOKEN_PATTERN.matcher(command).matches()) {
			return null;
		}
		String ppaddictName = backend.tryLinkToPpaddict(command, apiUser);
		if(ppaddictName == null)
			throw new UserException("nothing happened.");
		else
			return new Success("linked to " + ppaddictName);
	}

}
