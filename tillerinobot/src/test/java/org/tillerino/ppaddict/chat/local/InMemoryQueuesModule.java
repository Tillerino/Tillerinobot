package org.tillerino.ppaddict.chat.local;

import dagger.Binds;
import dagger.Module;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;

@Module
public interface InMemoryQueuesModule {
    @Binds
    GameChatEventQueue gameChatEventQueue(LocalGameChatEventQueue localGameChatEventQueue);

    @Binds
    GameChatResponseQueue gameChatResponseQueue(LocalGameChatResponseQueue localGameChatResponseQueue);
}
