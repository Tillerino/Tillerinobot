package org.tillerino.ppaddict.chat.local;

import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;

import com.google.inject.AbstractModule;

public class InMemoryQueuesModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(GameChatEventQueue.class).to(LocalGameChatEventQueue.class);
		bind(GameChatResponseQueue.class).to(LocalGameChatResponseQueue.class);
		bind(GameChatMetrics.class).to(LocalGameChatMetrics.class);
	}
}
