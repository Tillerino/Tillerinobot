package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;

import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.rabbit.RemoteResponseQueue;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.rabbitmq.client.Connection;

public class RabbitQueuesModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(GameChatMetrics.class).to(LocalGameChatMetrics.class);
	}

	@Provides
	GameChatEventQueue remoteQueue(Connection connection, MessageHandlerScheduler scheduler) throws IOException {
		RemoteEventQueue queue = RabbitMqConfiguration.eventQueue(connection);
		queue.setup();
		queue.subscribe(scheduler::onEvent);
		return queue;
	}

	@Provides
	GameChatResponseQueue responseQueue(Connection connection, ResponsePostprocessor post) throws IOException {
		RemoteResponseQueue queue = RabbitMqConfiguration.responseQueue(connection);
		queue.setup();
		queue.subscribe(response -> {
			try (MdcAttributes mdc = response.getEvent().getMeta().getMdc().apply()) {
				post.onResponse(response.getResponse(), response.getEvent());
			}
		});
		return queue;
	}

	@Provides
	LiveActivity liveActicity(Connection connection) throws IOException {
		return RabbitMqConfiguration.liveActivity(connection);
	}
}
