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
	 * @param cause the event that caused the reaction
	 */
	void message(String response, GameChatEvent cause) throws InterruptedException, IOException;

	/**
	 * Responds with an "action", a special kind of direct message.
	 *
	 * @param response the action to send
	 * @param cause the event that caused the reaction
	 */
	void action(String response, GameChatEvent cause) throws InterruptedException, IOException;
}
