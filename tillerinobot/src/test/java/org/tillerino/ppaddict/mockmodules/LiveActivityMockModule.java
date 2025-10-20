package org.tillerino.ppaddict.mockmodules;

import static org.mockito.Mockito.mock;

import dagger.Provides;
import javax.inject.Singleton;
import org.tillerino.ppaddict.chat.LiveActivity;

@dagger.Module
public interface LiveActivityMockModule {
    @Provides
    @Singleton
    static LiveActivity l() {
        return mock(LiveActivity.class);
    }
}
