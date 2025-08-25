package org.tillerino.ppaddict.chat.local;

import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;

import dagger.Binds;
import dagger.Module;

@Module
public interface InMemoryQueuesModule {
	@Binds
	GameChatEventQueue gameChatEventQueue(LocalGameChatEventQueue localGameChatEventQueue);

	@Binds
	GameChatResponseQueue gameChatResponseQueue(LocalGameChatResponseQueue localGameChatResponseQueue);
}
