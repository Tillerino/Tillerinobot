package org.tillerino.ppaddict.mockmodules;

import static org.mockito.Mockito.mock;

import dagger.Provides;
import javax.inject.Singleton;
import tillerino.tillerinobot.rest.BeatmapsService;

@dagger.Module
public interface BeatmapsServiceMockModule {
    @Provides
    @Singleton
    static BeatmapsService l() {
        return mock(BeatmapsService.class);
    }
}
