package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;

import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitRpc;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.rabbit.RemoteLiveActivity;
import org.tillerino.ppaddict.rabbit.RemoteResponseQueue;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.rabbitmq.client.Connection;

public class RabbitQueuesModule extends AbstractModule {
	@Override
	protected void configure() {
	}

	@Provides
	GameChatEventQueue internalEventQueue(Connection connection, MessageHandlerScheduler scheduler) throws IOException {
		RemoteEventQueue queue = RabbitMqConfiguration.internalEventQueue(connection);
		queue.setup();
		queue.subscribe(scheduler::onEvent);
		return queue;
	}

	@Provides
	GameChatResponseQueue responseQueue(Connection connection, ResponsePostprocessor post) throws IOException {
		RemoteResponseQueue queue = RabbitMqConfiguration.responseQueue(connection);
		queue.setup();
		queue.subscribe(response -> {
			try (var _ = response.getEvent().getMeta().getMdc().apply();
					var _ = response.getEvent().getMeta().getTimer().pinToThread()) {
				post.onResponse(response.getResponse(), response.getEvent());
			}
		});
		return queue;
	}

	@Provides
	LiveActivity liveActivity(Connection connection) throws IOException {
		RemoteLiveActivity liveActivity = RabbitMqConfiguration.liveActivity(connection);
		liveActivity.setup();
		return liveActivity;
	}

	@Provides
	GameChatWriter gameChatWriter(Connection connection) throws IOException {
		// !!! This module doesn't automatically start the server side !!!
		return RabbitRpc.remoteCallProxy(connection, GameChatWriter.class, new GameChatWriter.Error.Timeout());
	}

	@Provides
	GameChatClient gameChatClient(Connection connection) throws IOException {
		// !!! This module doesn't automatically start the server side !!!
		return RabbitRpc.remoteCallProxy(connection, GameChatClient.class, new GameChatClient.Error.Timeout());
	}
}
