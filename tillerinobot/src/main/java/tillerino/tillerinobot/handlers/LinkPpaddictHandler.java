package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

@RequiredArgsConstructor
public class LinkPpaddictHandler implements CommandHandler {
	public static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-z]{32}");
	private static final SecureRandom random = new SecureRandom();

	private final BotBackend backend;

	@Override
	public GameChatResponse handle(String command, OsuApiUser apiUser, UserData userData, Language lang)
			throws UserException, IOException, SQLException {
		if(!TOKEN_PATTERN.matcher(command).matches()) {
			return null;
		}
		String linkedName = backend.tryLinkToPatreon(command, apiUser);
		if(linkedName == null)
			throw new UserException("nothing happened.");
		else
			return new Success("linked to " + linkedName);
	}

	public static synchronized String newKey() {
		return StringUtils.leftPad(new BigInteger(165, LinkPpaddictHandler.random).toString(36), 32, '0');
	}

}
