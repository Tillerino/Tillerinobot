package org.tillerino.ppaddict.rabbit;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.util.MdcUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteResponseQueue extends AbstractRemoteQueue<RemoteResponseQueue.EventReponsePair> implements GameChatResponseQueue {

	RemoteResponseQueue(ObjectMapper mapper, Channel channel, String exchange, String queue) {
		super(mapper, channel, exchange, queue, log, EventReponsePair.class, null);
	}

	@Override
	public void onResponse(GameChatResponse response, GameChatEvent event) {
		event.getMeta().setMdc(MdcUtils.getSnapshot());
		send(new EventReponsePair(event, response));
	}

	@Override
	public int size() {
		return super.size();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EventReponsePair {
		GameChatEvent event;
		GameChatResponse response;
	}
}