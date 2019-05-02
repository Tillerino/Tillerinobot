package org.tillerino.ppaddict.chat;

import tillerino.tillerinobot.CommandHandler.Response;

public interface GameChatResponseConsumer {
	/**
	 * Handles a response to a game chat event.
	 *
	 * @param response the response to the event
	 * @param event the triggering event
	 */
	void onResponse(Response response, GameChatEvent event) throws InterruptedException;
}
