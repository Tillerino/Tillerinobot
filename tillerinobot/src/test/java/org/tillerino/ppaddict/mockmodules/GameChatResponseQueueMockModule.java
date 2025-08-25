package org.tillerino.ppaddict.mockmodules;

import static org.mockito.Mockito.mock;

import javax.inject.Singleton;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;

import dagger.Provides;

@dagger.Module
public interface GameChatResponseQueueMockModule {
  @Provides
  @Singleton
  static GameChatResponseQueue l() {
    return mock(GameChatResponseQueue.class);
  }
}
