package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * This is used twice:
 * - to get events from the IRC module to the main module (queue "irc-reader")
 * - to queue events internally (queue "game-chat-events")
 */
@Slf4j
public class RemoteEventQueue extends AbstractRemoteQueue<GameChatEvent> implements GameChatEventQueue {

	RemoteEventQueue(ObjectMapper mapper, Channel channel, String exchange, String queue) {
		super(mapper, channel, exchange, queue, log, GameChatEvent.class, 3);
	}

	@Override
	public void onEvent(GameChatEvent event) {
		send(event, event.getPriority());
	}

	@Override
	public int size() {
		return super.size();
	}
}
