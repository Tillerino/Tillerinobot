package org.tillerino.ppaddict.chat;

import org.tillerino.ppaddict.util.TidyObject;

/**
 * This is the "main function" of the chat bot, i.e. connects to the server,
 * starts accepting messages...
 */
public interface GameChatClient extends TidyObject, Runnable {
	boolean isConnected();

	/**
	 * Disconnects from the server in a soft manner. I.e. logs out / quits
	 * orderly. A "hard" disconnect would just close any open socket
	 * connections.
	 */
	void disconnectSoftly();
}