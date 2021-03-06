package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.util.MdcUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteEventQueue extends AbstractRemoteQueue<GameChatEvent> implements GameChatEventQueue {

	RemoteEventQueue(ObjectMapper mapper, Channel channel, String exchange, String queue) {
		super(mapper, channel, exchange, queue, log, GameChatEvent.class, 3);
	}

	@Override
	public void onEvent(GameChatEvent event) {
		event.getMeta().setMdc(MdcUtils.getSnapshot());
		int priority = 1;
		if (event.isInteractive()) {
			priority = 3;
		} else if (event instanceof Joined) {
			priority = 2;
		}
		send(event, priority);
	}

	@Override
	public int size() {
		return super.size();
	}
}
