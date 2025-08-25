package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;
import java.io.UncheckedIOException;

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

import com.rabbitmq.client.Connection;

import javax.inject.Singleton;

import dagger.Module;

@Module
public interface RabbitQueuesModule {
	@dagger.Provides
  @Singleton
	static GameChatEventQueue internalEventQueue(Connection connection, MessageHandlerScheduler scheduler) {
    try {
      RemoteEventQueue queue = RabbitMqConfiguration.internalEventQueue(connection);
      queue.setup();
      queue.subscribe(scheduler::onEvent);
      return queue;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

	@dagger.Provides
  @Singleton
  static GameChatResponseQueue responseQueue(Connection connection, ResponsePostprocessor post) {
    try {
      RemoteResponseQueue queue = RabbitMqConfiguration.responseQueue(connection);
      queue.setup();
      queue.subscribe(response -> {
        try (var _ = response.getEvent().getMeta().getMdc().apply();
            var _ = response.getEvent().getMeta().getTimer().pinToThread()) {
          post.onResponse(response.getResponse(), response.getEvent());
        }
      });
      return queue;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @dagger.Provides
  @Singleton
  static LiveActivity liveActivity(Connection connection) {
    try {
      RemoteLiveActivity liveActivity = RabbitMqConfiguration.liveActivity(connection);
      liveActivity.setup();
      return liveActivity;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @dagger.Provides
  @Singleton
  static GameChatWriter gameChatWriter(Connection connection) {
		// !!! This module doesn't automatically start the server side !!!
		return RabbitRpc.remoteCallProxy(connection, GameChatWriter.class, new GameChatWriter.Error.Timeout());
	}

  @dagger.Provides
  @Singleton
  static GameChatClient gameChatClient(Connection connection) {
		// !!! This module doesn't automatically start the server side !!!
		return RabbitRpc.remoteCallProxy(connection, GameChatClient.class, new GameChatClient.Error.Timeout());
	}
}
