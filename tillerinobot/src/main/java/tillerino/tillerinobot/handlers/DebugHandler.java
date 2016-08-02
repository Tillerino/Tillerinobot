package tillerino.tillerinobot.handlers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import org.tillerino.osuApiModel.OsuApiUser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;

@Value
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SuppressFBWarnings("TQ")
public class DebugHandler implements CommandHandler {
	private final BotBackend backend;
	
	private final IrcNameResolver resolver;

	static final String DEBUG = "debug ";

	@Override
	public boolean handle(String debugCommand, IRCBotUser debugIrcUser,
			OsuApiUser debugApiUser, UserData debugUserData)
			throws UserException, IOException, SQLException,
			InterruptedException {
		if (!debugCommand.startsWith(DEBUG)
				|| !debugUserData.isAllowedToDebug()) {
			return false;
		}
		try {
			CommandHandler commands = CommandHandler
					.alwaysHandling(
							"resolve ",
							(command, ircUser, apiUser, userData) -> {
								ircUser.message(command + " resolves to "
										+ resolver.resolveIRCName(command));
							})
					.or(CommandHandler.alwaysHandling(
							"getUserByIdCached ",
							(command, ircUser, apiUser, userData) -> {
								ircUser.message(command
										+ " is "
										+ backend.getUser(
												Integer.parseInt(command), 0l));
							}))
					.or(CommandHandler.alwaysHandling(
							"getUserByIdFresh ",
							(command, ircUser, apiUser, userData) -> {
								ircUser.message(command
										+ " is "
										+ backend.getUser(
												Integer.parseInt(command), 1l));
							}));
			if (commands.handle(debugCommand.substring(DEBUG.length()),
					debugIrcUser, debugApiUser, debugUserData)) {
				return true;
			}
			throw new UserException(debugUserData.getLanguage().invalidChoice(
					debugCommand, DEBUG + commands.getChoices()));
		} catch (UserException e) {
			throw e;
		} catch (Exception e) {
			debugIrcUser.message("An exception of type "
					+ e.getClass().getSimpleName() + " occurred: "
					+ e.getMessage());
			return true;
		}
	}

}
