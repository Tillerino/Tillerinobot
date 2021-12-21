package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;

@Value
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SuppressFBWarnings("TQ")
public class DebugHandler implements CommandHandler {
	private final BotBackend backend;

	private final IrcNameResolver resolver;

	static final String DEBUG = "debug ";

	@Override
	public GameChatResponse handle(String debugCommand, OsuApiUser debugApiUser,
			UserData debugUserData, Language lang) throws UserException, IOException,
			SQLException, InterruptedException {
		if (!debugCommand.startsWith(DEBUG)
				|| !debugUserData.isAllowedToDebug()) {
			return null;
		}
		try {
			CommandHandler commands = CommandHandler
					.alwaysHandling(
							"resolve ",
							(command, apiUser, userData, l) -> new Success(command
									+ " resolves to "
									+ resolver.resolveIRCName(command)))
					.or(CommandHandler.alwaysHandling(
							"getUserByIdCached ",
							(command, apiUser, userData, l) -> new Success(command
									+ " is "
									+ backend.getUser(
											Integer.parseInt(command), 0l))))
					.or(CommandHandler.alwaysHandling(
							"getUserByIdFresh ",
							(command, apiUser, userData, l) -> new Success(command
									+ " is "
									+ backend.getUser(
											Integer.parseInt(command), 1l))));
			GameChatResponse response = commands.handle(
					debugCommand.substring(DEBUG.length()), debugApiUser,
					debugUserData, lang);
			if (response != null) {
				return response;
			}
			throw new UserException(lang.invalidChoice(debugCommand, DEBUG + commands.getChoices()));
		} catch (UserException e) {
			throw e;
		} catch (Exception e) {
			return new Message("An exception of type "
					+ e.getClass().getSimpleName() + " occurred: "
					+ e.getMessage());
		}
	}

}
