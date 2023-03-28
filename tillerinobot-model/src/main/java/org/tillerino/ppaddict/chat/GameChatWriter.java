package org.tillerino.ppaddict.chat;

import java.io.IOException;

/**
 * Writes a response to an event to the game chat.
 */
public interface GameChatWriter {
	/**
	 * Responds with a direct message.
	 *
	 * @param response the message to send
	 * @param recipient the recipient of the message
	 */
	void message(String response, @IRCName String recipient) throws InterruptedException, IOException;

	/**
	 * Responds with an "action", a special kind of direct message.
	 *
	 * @param response the action to send
	 * @param recipient the recipient of the action
	 */
	void action(String response, @IRCName String recipient) throws InterruptedException, IOException;
}
