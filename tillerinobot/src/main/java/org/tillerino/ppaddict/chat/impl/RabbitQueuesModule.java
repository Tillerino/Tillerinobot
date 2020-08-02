package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;

import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.rabbit.RemoteResponseQueue;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitQueuesModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(GameChatMetrics.class).to(LocalGameChatMetrics.class);
		bind(GameChatEventQueue.class).to(RemoteEventQueue.class);
		bind(GameChatResponseQueue.class).to(RemoteResponseQueue.class);
	}

	@Provides
	RemoteEventQueue remoteQueue(Channel channel, MessageHandlerScheduler scheduler) throws IOException {
		RemoteEventQueue queue = RabbitMqConfiguration.eventQueue(channel);
		queue.setup();
		queue.subscribe(event -> {
			try {
				scheduler.onEvent(event);
			} catch (InterruptedException e) {
				log.warn("Interrupted. Ignoring event {}.", event.getEventId());
				Thread.currentThread().interrupt();
			}
		});
		return queue;
	}

	@Provides
	RemoteResponseQueue responseQueue(Channel channel, ResponsePostprocessor post) throws IOException {
		RemoteResponseQueue queue = RabbitMqConfiguration.responseQueue(channel);
		queue.setup();
		queue.subscribe(response -> {
			try {
				post.onResponse(response.getResponse(), response.getEvent());
			} catch (InterruptedException e) {
				log.warn("Interrupted. Ignoring response to {}.", response.getEvent().getEventId());
				Thread.currentThread().interrupt();
			}
		});
		return queue;
	}
}
