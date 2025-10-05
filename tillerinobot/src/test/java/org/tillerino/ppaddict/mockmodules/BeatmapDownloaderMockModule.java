package org.tillerino.ppaddict.mockmodules;

import javax.inject.Singleton;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.mockito.Mockito;

import dagger.Provides;
import org.tillerino.MockServerRule;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@dagger.Module
public interface BeatmapDownloaderMockModule {
  @Provides
  @Singleton
  static BeatmapDownloader l() {
    BeatmapDownloader wireMockBeatmapDownloader = WebResourceFactory.newResource(
        BeatmapDownloader.class,
        JerseyClientBuilder.createClient().target(MockServerRule.getExternalMockServerAddress()));
    return Mockito.spy(wireMockBeatmapDownloader);
  }
}
