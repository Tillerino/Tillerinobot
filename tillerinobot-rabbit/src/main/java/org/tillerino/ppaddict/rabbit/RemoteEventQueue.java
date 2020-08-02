package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.util.MdcUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteEventQueue extends AbstractRemoteQueue<GameChatEvent> implements GameChatEventQueue {

	RemoteEventQueue(ObjectMapper mapper, Channel channel, String exchange, String queue) {
		super(mapper, channel, exchange, queue, log, GameChatEvent.class);
	}

	@Override
	public void onEvent(GameChatEvent event) {
		event.getMeta().setMdc(MdcUtils.getSnapshot());
		send(event);
	}

	@Override
	public int size() {
		return super.size();
	}
}
