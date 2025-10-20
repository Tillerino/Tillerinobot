package org.tillerino.ppaddict.chat;

public interface GameChatResponseConsumer {
    /**
     * Handles a response to a game chat event.
     *
     * @param response the response to the event
     * @param event the triggering event
     */
    void onResponse(GameChatResponse response, GameChatEvent event) throws InterruptedException;
}
