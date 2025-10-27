package tillerino.tillerinobot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.github.omkelderman.sandoku.ProcessorApi;
import dagger.Lazy;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import org.mockito.Mockito;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.config.DatabaseConfigService;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.rest.AuthenticationServiceImpl;
import org.tillerino.ppaddict.util.Clock;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.recommendations.AllRecommenders;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.BeatmapsService;
import tillerino.tillerinobot.rest.BeatmapsServiceImpl;

/**
 * This module binds services to their real implementations while spying on them. This is a low-effort way of setting up
 * tests while still being able to mock and verify behaviour.
 *
 * <p>APIs are implemented against the wiremock container.
 *
 * <p>{@link BotBackend} is mocked.
 *
 * <p>The standard {@link Recommender} is mocked without any stubs.
 */
@dagger.Module
public interface TestBaseModule {

    @Provides
    @Singleton
    static OsuApi osuApi(OsuApiV2Sometimes osuApiV2Sometimes) {
        return spy(osuApiV2Sometimes);
    }

    @Provides
    @Singleton
    static BeatmapsLoader beatmapsLoader(BeatmapsLoaderImpl impl) {
        return spy(impl);
    }

    @Provides
    @Singleton
    static AuthenticationService authenticationService(AuthenticationServiceImpl impl) {
        return spy(impl);
    }

    @Provides
    @Singleton
    static BeatmapsService beatmapsService(BeatmapsServiceImpl impl) {
        return spy(impl);
    }

    @Provides
    @Singleton
    static BotBackend mockBotBackend() {
        return Mockito.mock(BotBackend.class);
    }

    @Provides
    @Singleton
    static Recommender recommender(AllRecommenders allRecommenders) {
        return spy(allRecommenders);
    }

    @Provides
    @Singleton
    static ConfigService configService(DatabaseConfigService databaseConfigService) {
        return spy(databaseConfigService);
    }

    @Provides
    @Singleton
    static DiffEstimateProvider diffEstimateProvider(BeatmapsService b, OsuApi d, ProcessorApi c, DatabaseManager dbm) {
        return spy(new DiffEstimateProvider(b, d, c, dbm));
    }

    @Provides
    @Singleton
    static OsutrackDownloader osutrackDownloader() {
        return spy(new TestOsutrackDownloader());
    }

    @Provides
    @Singleton
    static PlayerService playerService(DatabaseManager dbm) {
        return spy(new PlayerService(dbm));
    }

    @Provides
    @Singleton
    static PullThrough pullThrough(DatabaseManager dbm, Lazy<OsuApi> osuApi) {
        return spy(new PullThrough(dbm, osuApi));
    }

    @Provides
    @Singleton
    static RecommendationsManager recommendationsManager(
            DatabaseManager dbm,
            RecommendationRequestParser parser,
            BeatmapsLoader beatmapsLoader,
            Recommender recommender,
            OsuApi osuApi,
            Clock clock,
            ConfigService config,
            DiffEstimateProvider diffEstimateProvider) {
        return spy(new RecommendationsManager(
                dbm, parser, beatmapsLoader, recommender, osuApi, clock, config, diffEstimateProvider));
    }

    @Provides
    @Singleton
    @Named("standard")
    static Recommender standardRecommender() {
        return mock(Recommender.class);
    }
}
