package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;

import lombok.AllArgsConstructor;
import lombok.Value;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;

@Value
@AllArgsConstructor(onConstructor=@__({@Inject}))
public class LinkPpaddictHandler implements CommandHandler {
	public static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-z]{32}");

	private final BotBackend backend;
	private final AbstractPpaddictUserDataService<?> ppaddictUserDataService;

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData)
			throws UserException, IOException, SQLException {
		if(!TOKEN_PATTERN.matcher(command).matches()) {
			return null;
		}
		String linkedName = ppaddictUserDataService.tryLinkToPpaddict(command, apiUser.getUserId())
				.orElseGet(() -> backend.tryLinkToPatreon(command, apiUser));
		if(linkedName == null)
			throw new UserException("nothing happened.");
		else
			return new Success("linked to " + linkedName);
	}

}
