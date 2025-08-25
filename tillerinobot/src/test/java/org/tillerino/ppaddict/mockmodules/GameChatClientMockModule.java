package org.tillerino.ppaddict.mockmodules;

import static org.mockito.Mockito.mock;

import javax.inject.Singleton;
import org.tillerino.ppaddict.chat.GameChatClient;

import dagger.Provides;

@dagger.Module
public interface GameChatClientMockModule {

  @Provides
  @Singleton
  static GameChatClient l() {
    return mock(GameChatClient.class);
  }
}
