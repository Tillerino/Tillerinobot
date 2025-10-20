package org.tillerino.ppaddict.chat;

/**
 * There are three consumers of chat events: the synchronous consumer, the queue, and the asynchronous consumer:
 *
 * <p>- The synchronous consumer is responsible for enqueueing the received messages, skipping messages from over-eager
 * users and propagating to the live UI. - The queue enqueues chat events for asynchronous consumption. - The
 * asynchronous consumer is then fed by the queue and doing the actual processing.
 */
public interface GameChatEventConsumer {
    void onEvent(GameChatEvent event) throws InterruptedException;
}
