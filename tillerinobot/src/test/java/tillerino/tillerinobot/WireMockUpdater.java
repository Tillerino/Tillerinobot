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
import org.apache.commons.lang3.tuple.Pair;

import lombok.SneakyThrows;
import tillerino.tillerinobot.OsuApiV1.DownloaderModule;

/**
 * See {@link #main(String[])}
 */
class WireMockUpdater {

  @dagger.Component(modules = UpdateModule.class)
  @Singleton
  interface UpdateInjector {
    void inject(WireMockUpdater o);
  }

  @dagger.Component(modules = CheckModule.class)
  @Singleton
  interface CheckInjector {
    void inject(WireMockUpdater o);
  }

  @dagger.Module
  record UpdateModule(String wireMockBase) {
    UpdateModule {
      if (!wireMockBase.endsWith("/")) {
        throw new IllegalArgumentException("osu API URL must end with a slash");
      }
    }

    @dagger.Provides
    @Named("osuapi.url")
    @SneakyThrows
    URL getOsuApiUrl() {
      return URI.create(wireMockBase).toURL();
    }

    @dagger.Provides
    @Named("osuapi.key")
    static String getOsuApiKey() {
      return DownloaderModule.getOsuApiKey();
    }
  }

  @dagger.Module
  record CheckModule(String wireMockBase) {
    CheckModule {
      if (!wireMockBase.endsWith("/")) {
        throw new IllegalArgumentException("osu API URL must end with a slash");
      }
    }

    @dagger.Provides
    @Named("osuapi.url")
    @SneakyThrows
    URL getOsuApiUrl() {
      return URI.create(wireMockBase).toURL();
    }

    @dagger.Provides
    @Named("osuapi.key")
    static String getOsuApiKey() {
      return OsuApiV1Test.OSUAPI_V1_MOCK_KEY;
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


  @Inject
  OsuApiV1 api;

  @Inject
  RateLimiter rateLimiter;

  @Inject
  @Named("osuapi.key")
  String osuApiV1Key;

  private void updateMocks() throws IOException {
    DaggerWireMockUpdater_UpdateInjector.builder().updateModule(new UpdateModule(wireMockServer.baseUrl() + "/api/")).build().inject(this);

    wireMockServer.startRecording(new RecordSpecBuilder()
        .forTarget("https://osu.ppy.sh")
        .transformers("removeCredentials")
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
        Pair.of(osuApiV1Key, OsuApiV1Test.OSUAPI_V1_MOCK_KEY)
    );
    for (File mappingFile : new File("src/test/wiremock/mappings").listFiles()) {
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
    DaggerWireMockUpdater_CheckInjector.builder().checkModule(new CheckModule(wireMockServer.baseUrl() + "/api/")).build().inject(this);

    callRoutes();
  }

  private void callRoutes() throws IOException {
    rateLimiter.startSchedulingPermits(Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory()));
    rateLimiter.setThreadPriority(RateLimiter.REQUEST);

    // System.out.println(api.getUser(2070907, 0));
    // System.out.println(api.getUserTop(2070907, 0, 100));
    System.out.println(api.getUserTop(2070907, 0, 50));
    // System.out.println(api.getUser("Tillerino", 0));
    // System.out.println(api.getUserRecent(2070907, 0));
    // System.out.println(api.getBeatmap(131891, 0));
  }

  /**
   * Updates or checks mocks. When you update mocks, maybe comment out those mocks you don't really want to update.
   * Make sure this runs in the `tillerinobot` module, otherwise the correct paths are not found.
   * Reads credentials for APIs from environment variables:
   * OSUAPIKEY
   *
   */
  public static void main(String[] args) throws Exception {
    WireMockUpdater updater = new WireMockUpdater();

    updater.updateMocks();

    updater.wireMockServer.stop();
  }
}