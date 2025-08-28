package org.tillerino.ppaddict;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.tillerino.osuApiModel.v2.DownloaderV2;
import org.tillerino.osuApiModel.v2.TokenHelper.Credentials;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.impl.RabbitQueuesModule;
import org.tillerino.ppaddict.config.CachedDatabaseConfigServiceModule;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.rest.AuthenticationServiceImpl;
import org.tillerino.ppaddict.util.Clock;

import dagger.Binds;
import dagger.Provides;
import lombok.SneakyThrows;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.BotBackend.BeatmapsLoader;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.recommendations.AllRecommenders;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;
import tillerino.tillerinobot.rest.BeatmapsServiceImpl;

/**
 * This sets up the configuration for production.
 * It is referenced outside of this project.
 *
 * <p>Only realized the Factorio-reference just now, so this shall never be renamed.
 */
@dagger.Module(includes = {
    RabbitQueuesModule.class,
    ProcessorsModule.class,
    CachedDatabaseConfigServiceModule.class,
    BeatmapsServiceImpl.Module.class,
    OsuApiV1.DownloaderModule.class,
    OsuApiV2.CredentialsFromEnvModule.class,
    MessageHandlerSchedulerModule.class,
    Clock.Module.class
})
public interface ProdModule {
  @Binds
  BeatmapsLoader beatmapsLoader(BeatmapsLoaderImpl beatmapsLoader);

  @Binds
  AuthenticationService authenticationService(AuthenticationServiceImpl authenticationService);

  @Binds
  OsuApi osuApi(OsuApiV2Sometimes osuApiV2Sometimes);

  @Binds
  Recommender recommender(AllRecommenders allRecommenders);

  @Provides
  static SanDoku sanDoku() {
    return SanDoku.defaultClient(URI.create(Optional.ofNullable(System.getenv("SAN_DOKU_URL")).orElse("http://san-doku:8080")));
  }

  @Provides
  static BeatmapDownloader beatmapDownloader() {
    return WebResourceFactory.newResource(BeatmapDownloader.class,
        JerseyClientBuilder.createClient().target("https://osu.ppy.sh"));
  }

  @Provides
  @Named("coreSize")
  static int coreSize() {
    return 8;
  }

  @Provides
  static @Named("nap.url") URI napUri() {
    return URI.create("https://osu.kpei.me/api/recommend/tillerino");
  }

  @Provides
  static @Named("nap.token") String napToken() {
    return env("NAP_TOKEN")
        .orElseThrow(() -> new NoSuchElementException("NAP_TOKEN must be configured!"));
  }

  @Provides
  static @Named("ppaddict.url") String ppaddictUrl() {
    return env("PPADDICT_URL")
        .orElseThrow(() -> new NoSuchElementException("PPADDICT_URL must be configured!"));
  }

  @Provides
  static @Named("ppaddict.auth.url") String ppaddictAuthUrl() {
    return env("PPADDICT_AUTH_URL")
        .orElseThrow(() -> new NoSuchElementException("PPADDICT_AUTH_URL must be configured!"));
  }

  @Provides
  @Singleton
  @SneakyThrows
  static Connection rabbitMqConnection(ExecutorService exec) {
    ConnectionFactory connectionFactory = RabbitMqConfiguration.connectionFactory(
        env("RABBIT_HOST").orElse("rabbitmq"),
        env("RABBIT_PORT").map(Integer::valueOf).orElse(5672),
        env("RABBIT_VHOST").orElse("/")
    );
    connectionFactory.setSharedExecutor(exec);
    return connectionFactory.newConnection("fattie");
  }

  static Optional<String> env(String name) {
    String value = System.getProperty(name.toLowerCase().replace('_', '.'), System.getenv(name));
    return Optional.ofNullable(value).filter(StringUtils::isNotBlank);
  }
}
