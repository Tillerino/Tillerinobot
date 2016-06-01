package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.IRCBotUser;
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
		 * @param ircUser
		 *            the requesting user's irc object.
		 * @param apiUser
		 *            the requesting user's api object.
		 * @param userData
		 *            the requesting user's data.
		 * @throws UserException
		 *             if the input is invalid
		 */
		public void handle(String command, IRCBotUser ircUser,
				OsuApiUser apiUser, UserData userData) throws UserException,
				IOException, SQLException, InterruptedException;
	}

	/**
	 * 
	 * @param command
	 *            the command <i>excluding</i> the leading exclamation mark if
	 *            there was one.
	 * @param ircUser
	 *            the requesting user's irc object.
	 * @param apiUser
	 *            the requesting user's api object.
	 * @param userData
	 *            the requesting user's data.
	 * @return true if the input matched this handler.
	 * @throws UserException
	 *             if the input is invalid
	 */
	public boolean handle(String command, IRCBotUser ircUser,
			OsuApiUser apiUser, UserData userData) throws UserException,
			IOException, SQLException, InterruptedException;

	public default CommandHandler or(CommandHandler next) {
		CommandHandler me = this;
		return new CommandHandler() {
			@Override
			public boolean handle(String command, IRCBotUser ircUser,
					OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				if (me.handle(command, ircUser, apiUser, userData)) {
					return true;
				}
				return next.handle(command, ircUser, apiUser, userData);
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
	 * @param handler
	 *            the handler to be called if the message starts with the given
	 *            string. the remaining string will be passed to this handler.
	 * @return the modified handler.
	 */
	public static CommandHandler handling(String start,
			CommandHandler underlying) {
		return new CommandHandler() {
			@Override
			public boolean handle(String command, IRCBotUser ircUser,
					OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				if (!StringUtils.startsWithIgnoreCase(command, start)) {
					return false;
				}
				if (!underlying.handle(command.substring(start.length()),
						ircUser, apiUser, userData)) {
					throw new UserException(userData.getLanguage()
							.invalidChoice(command, getChoices()));
				}
				return true;
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
	 * @param handler
	 *            the handler to be called if the message starts with the given
	 *            string. the remaining string will be passed to this handler.
	 * @return the modified handler.
	 */
	public static CommandHandler alwaysHandling(String start,
			AnyCommandHandler underlying) {
		return new CommandHandler() {
			@Override
			public boolean handle(String command, IRCBotUser ircUser,
					OsuApiUser apiUser, UserData userData)
					throws UserException, IOException, SQLException,
					InterruptedException {
				if (!StringUtils.startsWithIgnoreCase(command, start)) {
					return false;
				}
				underlying.handle(command.substring(start.length()), ircUser,
						apiUser, userData);
				return true;
			}

			@Override
			public String getChoices() {
				return start;
			}
		};
	}
}