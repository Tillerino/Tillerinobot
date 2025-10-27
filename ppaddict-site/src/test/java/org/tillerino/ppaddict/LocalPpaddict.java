package org.tillerino.ppaddict;

import dagger.Component;
import io.undertow.Undertow;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import org.tillerino.WireMockDocker;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.auth.FakeAuthenticatorService;
import org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.local.InMemoryQueuesModule;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.PpaddictUserDataService;
import org.tillerino.ppaddict.server.auth.AuthArriveService;
import org.tillerino.ppaddict.server.auth.implementations.OsuOauth;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.recommendations.Recommender;

/** Starts ppaddict locally on port 8080 with a fake backend. */
public class LocalPpaddict {
    @Inject
    PpaddictContextConfigurator configurator;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    @Inject
    Recommender standardRecommender;

    public static void main(String[] args) throws Exception {
        LocalPpaddict localPpaddict = new LocalPpaddict();
        DaggerLocalPpaddict_Injector.create().inject(localPpaddict);
        TestBase.mockBeatmapMetas(localPpaddict.diffEstimateProvider);
        TestBase.mockRecommendations(localPpaddict.standardRecommender);

        MysqlContainer.MysqlDatabaseLifecycle.createSchema();
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(PpaddictModule.createFilterPathHandler(deploymentInfo -> {
                    localPpaddict.configurator.configureUndertow(deploymentInfo);
                    PpaddictContextConfigurator.addServlet(deploymentInfo, "/showErrorPage", new ProducesError());
                    PpaddictContextConfigurator.addServlet(
                            deploymentInfo, FakeAuthenticatorWebsite.PATH, new FakeAuthenticatorWebsite());
                }))
                .build();
        server.start();
    }

    @Component(modules = Module.class)
    @Singleton
    interface Injector {
        void inject(LocalPpaddict t);
    }

    @dagger.Module(
            includes = {
                DockeredMysqlModule.class,
                InMemoryQueuesModule.class,
                LiveActivityMockModule.class,
                MessageHandlerSchedulerModule.class,
                ProcessorsModule.class,
                TestBaseModule.class,
                WireMockDocker.Module.class,
                TestClock.Module.class,
                FakeAuthenticatorService.Module.class
            })
    interface Module {
        @dagger.Provides
        static @Named("ppaddict.auth.returnURL") String returnUrl() {
            return "http://localhost:8080" + AuthArriveService.PATH;
        }

        @dagger.Provides
        static @Named("ppaddict.apiauth.key") String authKey() {
            return "ppaddict-app-key";
        }

        @dagger.Binds
        PpaddictBackend ppaddictBackend(TestBackend testBackend);

        @dagger.Provides
        @Singleton
        static PpaddictUserDataService getPpaddictUserDataService(
                DatabaseManager linkKeys, Clock clock, BotBackend botBackend, OsuApi osuApi, Recommender recommender) {
            final String osuOAuthPrefix = OsuOauth.OSU_AUTH_SERVICE_IDENTIFIER + ":";

            return new PpaddictUserDataService(linkKeys, clock) {
                @Override
                @SneakyThrows
                public String getLinkString(String id, String displayName) {
                    if (id.startsWith(osuOAuthPrefix)) {
                        int osuId = Integer.parseInt(id.substring(osuOAuthPrefix.length()));
                        MockData.mockUser(displayName, false, 100000, 1000, osuId, botBackend, osuApi, recommender);
                    }
                    return super.getLinkString(id, displayName);
                }
            };
        }
    }
}
