package org.tillerino.ppaddict.mockmodules;

import javax.inject.Singleton;
import org.mockito.Mockito;

import dagger.Provides;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@dagger.Module
public interface BeatmapDownloaderMockModule {
  @Provides
  @Singleton
  static BeatmapDownloader l() {
    return Mockito.mock(BeatmapDownloader.class);
  }
}
