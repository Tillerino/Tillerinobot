package tillerino.tillerinobot;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.chat.GameChatResponse;

import tillerino.tillerinobot.UserDataManager.UserData;

public interface CommandHandler {
	/**
	 * A special command handler, which will handle any input. It will at most
	 * throw a {@link UserException} if the input is somehow invalid.
	 */
	public interface AnyCommandHandler {
		/**
		 * 
		 * @param command
		 *            the command <i>excluding</i> the leading exclamation mark
		 *            if there was one.
		 * @param apiUser
		 *            the requesting user's api object.
		 * @param userData
		 *            the requesting user's data.
		 * @return null if the command was not handled
		 * @throws UserException
		 *             if the input is invalid
		 */
		@Nonnull
		public GameChatResponse handle(String command, OsuApiUser apiUser,
				UserData userData) throws UserException,
				IOException, SQLException, InterruptedException;
	}

	/**
	 * 
	 * @param command
	 *            the command <i>excluding</i> the leading exclamation mark if
	 *            there was one.
	 * @param apiUser
	 *            the requesting user's api object.
	 * @param userData
	 *            the requesting user's data.
	 * @return null if the command was not handled
	 * @throws UserException
	 *             if the input is invalid
	 */
	@CheckForNull
	public GameChatResponse handle(String command, OsuApiUser apiUser,
			UserData userData) throws UserException,
			IOException, SQLException, InterruptedException;

	public default CommandHandler or(CommandHandler next) {
		CommandHandler me = this;
		return new CommandHandler() {
			@Override
			public GameChatResponse handle(String command, OsuApiUser apiUser,
					UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				GameChatResponse response = me.handle(command, apiUser, userData);
				if (response != null) {
					return response;
				}
				return next.handle(command, apiUser, userData);
			}

			@Override
			public String getChoices() {
				return me.getChoices() + "|" + next.getChoices();
			}
		};
	}

	@Nonnull
	public default String getChoices() {
		return "(unknown)";
	}

	/**
	 * Returns a modified {@link CommandHandler}, which calls the underlying
	 * handler only if the incoming message starts with the given string. In
	 * this case, the remaining string is passed to the underlying handler.
	 * 
	 * @param start
	 *            only messages starting with this string are considered. case
	 *            ignored.
	 * @param underlying
	 *            the handler to be called if the message starts with the given
	 *            string. the remaining string will be passed to this handler.
	 * @return the modified handler.
	 */
	public static CommandHandler handling(String start,
			CommandHandler underlying) {
		return new CommandHandler() {
			@Override
			public GameChatResponse handle(String command, OsuApiUser apiUser,
					UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				if (!StringUtils.startsWithIgnoreCase(command, start)) {
					return null;
				}
				GameChatResponse response = underlying.handle(command.substring(start.length()),
						apiUser, userData);
				if (response != null) {
					return response;
				}
				throw new UserException(userData.getLanguage()
						.invalidChoice(command, getChoices()));
			}

			@Override
			public String getChoices() {
				return start
						+ (!underlying.getChoices().isEmpty() ? "("
								+ underlying.getChoices() + ")" : "");
			}
		};
	}

	/**
	 * Returns a modified {@link CommandHandler}, which calls the underlying
	 * handler only if the incoming message starts with the given string. In
	 * this case, the remaining string is passed to the underlying handler. The
	 * underlying handler is assumed to <i>always</i> handle the command.
	 * 
	 * @param start
	 *            only messages starting with this string are considered. case
	 *            ignored.
	 * @param underlying
	 *            the handler to be called if the message starts with the given
	 *            string. the remaining string will be passed to this handler.
	 * @return the modified handler.
	 */
	public static CommandHandler alwaysHandling(String start,
			AnyCommandHandler underlying) {
		return new CommandHandler() {
			@Override
			public GameChatResponse handle(String command, OsuApiUser apiUser,
					UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				if (!StringUtils.startsWithIgnoreCase(command, start)) {
					return null;
				}
				return underlying.handle(command.substring(start.length()), apiUser,
						userData);
			}

			@Override
			public String getChoices() {
				return start;
			}
		};
	}

	public abstract static class WithShorthand implements CommandHandler {
		private final String command;
		private final String alias;
		private final String aliasWithSpace;

		public WithShorthand(String command) {
			this.command = command;
			this.alias = String.valueOf(command.charAt(0));
			this.aliasWithSpace = new String(new char[]{command.charAt(0), ' '});
		}

		@Override
		public final GameChatResponse handle(String originalCommand, OsuApiUser apiUser, UserData userData) throws UserException,
				IOException, SQLException, InterruptedException {
			String lowerCase = originalCommand.toLowerCase();
			if (lowerCase.equals(alias)) {
				return handleArgument(originalCommand, "", apiUser, userData);
			}
			if (getLevenshteinDistance(lowerCase, command) <= 2) {
				return handleArgument(originalCommand, "", apiUser, userData);
			}
			if (lowerCase.startsWith(aliasWithSpace)) {
				return handleArgument(originalCommand, originalCommand.substring(2), apiUser, userData);
			}
			int pos = lowerCase.indexOf(' ');
			if (pos > 0 && getLevenshteinDistance(lowerCase.substring(0, pos), command) <= 2) {
				return handleArgument(originalCommand, originalCommand.substring(pos + 1), apiUser, userData);
			}
			return null;
		}

		public abstract GameChatResponse handleArgument(String originalCommand, @Nonnull String remaining, OsuApiUser apiUser, UserData userData)
				throws UserException, IOException, SQLException, InterruptedException;
	}
}