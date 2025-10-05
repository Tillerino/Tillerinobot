package tillerino.tillerinobot;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.tillerino.osuApiModel.v2.TokenHelper.Credentials;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;

import dagger.Provides;
import lombok.SneakyThrows;
import tillerino.tillerinobot.OsuApiV1.DownloaderModule;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

/**
 * See {@link #main(String[])}
 */
class WireMockUpdater {

  @dagger.Component(modules = {UpdateModule.class, UrlsModule.class})
  @Singleton
  interface UpdateInjector {
    void inject(WireMockUpdater o);
  }

  @dagger.Component(modules = {CheckModule.class, UrlsModule.class})
  @Singleton
  interface CheckInjector {
    void inject(WireMockUpdater o);
  }

  @dagger.Module
  record UrlsModule(String wireMockBase) {
    UrlsModule {
      if (wireMockBase.endsWith("/")) {
        throw new IllegalArgumentException("osu API URL must not end with a slash");
      }
    }

    @dagger.Provides
    @Named("osuapi.url")
    @SneakyThrows
    URL getOsuApiV1Url() {
      return URI.create(wireMockBase + "/api/").toURL();
    }

    @dagger.Provides
    @Named("osuapiv2.url")
    @SneakyThrows
    URI getOsuApiV2Url() {
      return URI.create(wireMockBase);
    }

    @Provides
    static TokenCache tokenCache(@Named("osuapiv2.url") URI baseUrl, Credentials credentials) {
      return TokenCache.inMemory(baseUrl, credentials);
    }

    @dagger.Provides
    BeatmapDownloader beatmapDownloader() {
      return WebResourceFactory.newResource(
          BeatmapDownloader.class,
          JerseyClientBuilder.createClient().target(wireMockBase));
    }
  }

  @dagger.Module
  record UpdateModule(String wireMockBase) {
    @dagger.Provides
    @Named("osuapi.key")
    static String getOsuApiKey() {
      return DownloaderModule.getOsuApiKey();
    }

    @Provides
    static Credentials clientId() {
      return OsuApiV2.CredentialsFromEnvModule.credentials();
    }
  }

  @dagger.Module
  record CheckModule(String wireMockBase) {

    @dagger.Provides
    @Named("osuapi.key")
    static String getOsuApiKey() {
      return OsuApiV1Test.OSUAPI_V1_MOCK_KEY;
    }

    @Provides
    static Credentials clientId() {
      return OsuApiV2Test.OSUAPI_V2_MOCK_CREDENTIALS;
    }
  }

  WireMockServer wireMockServer;
  {
    SingleRootFileSource fileSource = new SingleRootFileSource("src/test/wiremock");
    SingleRootFileSource mappingsFileSource = new SingleRootFileSource("src/test/wiremock/mappings");
    new File(fileSource.getPath() + "/__files").mkdirs();
    new File(mappingsFileSource.getPath()).mkdirs();
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
        .fileSource(fileSource)
        .mappingSource(new JsonFileMappingsSource(mappingsFileSource, new FilenameMaker())));
    wireMockServer.start();
  }


  @Inject OsuApiV1 osuApiV1;
  @Inject OsuApiV2 osuApiV2;
  @Inject RateLimiter rateLimiter;
  @Inject @Named("osuapi.key") String osuApiV1Key;
  @Inject Credentials credentials;
  @Inject BeatmapDownloader beatmapDownloader;

  @Inject TokenCache tokenCache;

  private void updateMocks() throws IOException {
    DaggerWireMockUpdater_UpdateInjector.builder().urlsModule(new WireMockUpdater.UrlsModule(wireMockServer.baseUrl())).build().inject(this);

    wireMockServer.startRecording(new RecordSpecBuilder()
        .forTarget("https://osu.ppy.sh")
        .captureHeader("Authorization")
        .ignoreRepeatRequests()
        .allowNonProxied(true));

    callRoutes();

    wireMockServer.stopRecording();

    replaceSecrets();
  }

  /**
   * Removes all our secrets from the mocks.
   * WireMock offers something similar, but I think it doesn't apply to requests, only to responses.
   */
  private void replaceSecrets() throws IOException {
    List<Pair<String, String>> replacements = List.of(
        Pair.of(osuApiV1Key, OsuApiV1Test.OSUAPI_V1_MOCK_KEY),
        Pair.of(credentials.clientId(), OsuApiV2Test.OSUAPI_V2_MOCK_CLIENT_ID),
        Pair.of(credentials.clientSecret(), OsuApiV2Test.OSUAPI_V2_MOCK_CLIENT_SECRET),
        Pair.of(tokenCache.getToken(), OsuApiV2Test.OSUAPI_V2_MOCK_TOKEN)
    );
    for (File mappingFile : ArrayUtils.addAll(
        new File("src/test/wiremock/mappings").listFiles(),
        new File("src/test/wiremock/__files").listFiles())) {
      String originalContent = FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8);
      String editedContent = originalContent;
      for (Pair<String, String> replacement : replacements) {
        editedContent = editedContent.replace(replacement.getLeft(), replacement.getRight());
      }
      if (!originalContent.equals(editedContent)) {
        FileUtils.write(mappingFile, editedContent, StandardCharsets.UTF_8);
      }
    }
  }

  private void checkMocks() throws IOException {
    DaggerWireMockUpdater_CheckInjector.builder().urlsModule(new WireMockUpdater.UrlsModule(wireMockServer.baseUrl())).build().inject(this);

    callRoutes();
  }

  private void callRoutes() throws IOException {
    rateLimiter.startSchedulingPermits(Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory()));
    rateLimiter.setThreadPriority(RateLimiter.REQUEST);

    // System.out.println(osuApiV1.getUser(2070907, 0));
    // System.out.println(osuApiV1.getUserTop(2070907, 0, 50));
    // System.out.println(osuApiV1.getUser("Tillerino", 0));
    // System.out.println(osuApiV1.getUserRecent(2070907, 0));
    // System.out.println(osuApiV1.getBeatmap(131891, 0));

    // System.out.println(osuApiV2.getUser(2070907, 0));
    // System.out.println(osuApiV2.getUserTop(2070907, 0, 50));
    // System.out.println(osuApiV2.getUser("Tillerino", 0));
    // System.out.println(osuApiV2.getUserRecent(2070907, 0));
    // System.out.println(osuApiV2.getBeatmap(131891, 0));
    System.out.println(osuApiV2.getBeatmap(131891, 16));
    // System.out.println(osuApiV2.getBeatmapTop(131891, 0));
    // beatmapDownloader.getActualBeatmap(131891);
  }

  /**
   * Updates or checks mocks. When you update mocks, maybe comment out those mocks you don't really want to update.
   * Make sure this runs in the `tillerinobot` module, otherwise the correct paths are not found.
   * Reads credentials for APIs from environment variables:
   * OSUAPIKEY (for v1)
   * OSU_API_CLIENT_ID, OSU_API_CLIENT_SECRET (for v2)
   */
  public static void main(String[] args) throws Exception {
    WireMockUpdater updater = new WireMockUpdater();

    updater.updateMocks();

    updater.wireMockServer.stop();
  }
}