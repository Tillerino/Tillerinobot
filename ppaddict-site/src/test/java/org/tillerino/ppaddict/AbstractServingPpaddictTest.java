package org.tillerino.ppaddict;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import dagger.Component;
import io.undertow.Undertow;
import java.net.InetSocketAddress;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.tillerino.WireMockDocker;
import org.tillerino.ppaddict.auth.FakeAuthenticatorService;
import org.tillerino.ppaddict.auth.FakeAuthenticatorWebsite;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.local.InMemoryQueuesModule;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.PpaddictUserDataService;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.recommendations.Recommender;

@Slf4j
public class AbstractServingPpaddictTest extends AbstractDatabaseTest {

    @Component(modules = AbstractServingPpaddictTest.Module.class)
    @Singleton
    interface Injector {

        void inject(AbstractServingPpaddictTest t);
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
            return "";
        }

        @dagger.Provides
        static @Named("ppaddict.apiauth.key") String authKey() {
            return "ppaddict-app-key";
        }

        @dagger.Binds
        PpaddictBackend ppaddictBackend(TestBackend testBackend);
    }

    @Inject
    PpaddictContextConfigurator configurator;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    @Inject
    Recommender standardRecommender;

    @Inject
    PpaddictUserDataService userDataService;

    @Inject
    BotBackend botBackend;

    @Inject
    OsuApi osuApi;

    @Inject
    PullThrough pullThrough;

    private Undertow server;

    @Getter
    private int port;

    {
        DaggerAbstractServingPpaddictTest_Injector.create().inject(this);
    }

    @BeforeEach
    void startUndertow() throws Exception {
        TestBase.mockBeatmapMetas(diffEstimateProvider);
        TestBase.mockRecommendations(standardRecommender);
        ApiUser apiUser = new ApiUser();
        apiUser.setUserId(12345);
        apiUser.setUserName("TestUser");
        apiUser.setRank(0);
        apiUser.setPp(0);
        apiUser.setCountry("XX");
        doReturn(apiUser).when(pullThrough).getUser(eq(12345), anyLong());
        doReturn(1).when(botBackend).getDonator(12345);
        server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(PpaddictModule.createFilterPathHandler(deploymentInfo -> {
                    configurator.configureUndertow(deploymentInfo);
                    PpaddictContextConfigurator.addServlet(deploymentInfo, "/showErrorPage", new ProducesError());
                    PpaddictContextConfigurator.addServlet(
                            deploymentInfo, FakeAuthenticatorWebsite.PATH, new FakeAuthenticatorWebsite());
                }))
                .build();
        server.start();
        port = ((InetSocketAddress) server.getListenerInfo().getFirst().getAddress()).getPort();
    }

    @AfterEach
    void stopUndertow() throws Exception {
        server.stop();
    }
}
