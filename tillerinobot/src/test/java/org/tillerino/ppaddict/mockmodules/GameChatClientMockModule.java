package org.tillerino.ppaddict.mockmodules;

import static org.mockito.Mockito.mock;

import dagger.Provides;
import javax.inject.Singleton;
import org.tillerino.ppaddict.chat.GameChatClient;

@dagger.Module
public interface GameChatClientMockModule {

    @Provides
    @Singleton
    static GameChatClient l() {
        return mock(GameChatClient.class);
    }
}
