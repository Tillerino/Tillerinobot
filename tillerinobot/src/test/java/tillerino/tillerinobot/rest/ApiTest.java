package tillerino.tillerinobot.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.tillerino.ppaddict.util.Result.ok;
import static org.tillerino.ppaddict.util.TestAppender.mdc;

import dagger.Binds;
import dagger.Component;
import dagger.Provides;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import nl.altindag.log.model.LogEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.ListAssert;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.mockmodules.BeatmapsServiceMockModule;
import org.tillerino.ppaddict.mockmodules.GameChatClientMockModule;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;
import tillerino.tillerinobot.FakeAuthenticationService;
import tillerino.tillerinobot.TestBackend;

/** Tests the Tillerinobot API on a live HTTP server including authentication. */
public class ApiTest {
    @Component(
            modules = {
                DockeredMysqlModule.class,
                Module.class,
                TestBackend.Module.class,
                TestClock.Module.class,
                BeatmapsServiceMockModule.class,
                GameChatClientMockModule.class
            })
    @Singleton
    interface Injector {
        void inject(ApiTest t);
    }

    @dagger.Module
    interface Module {
        @Singleton
        @Provides
        static JdkServerResource jdkServerResource(BotApiDefinition def) {
            return new JdkServerResource(def, "localhost", 0);
        }

        @Provides
        static @Named("tillerinobot.test.persistentBackend") boolean persistentBackend() {
            return false;
        }

        @Binds
        AuthenticationService authenticationService(FakeAuthenticationService fakeAuthenticationService);
    }

    {
        DaggerApiTest_Injector.create().inject(this);
    }

    /** Filter for the client to add a header or query parameter to the request. */
    static class SetHeaderAndParam implements ClientRequestFilter {
        /** Set this field to add a header to the request */
        private Entry<String, String> addToHeader = null;

        /** Set this field to add a param to the request */
        private Entry<String, String> addParam = null;

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            if (addToHeader != null) {
                requestContext.getHeaders().add(addToHeader.getKey(), addToHeader.getValue());
            }
            if (addParam != null) {
                try {
                    URI uri = requestContext.getUri();
                    uri = new URI(
                            uri.getScheme(),
                            uri.getUserInfo(),
                            uri.getHost(),
                            uri.getPort(),
                            uri.getPath(),
                            uri.getQuery() + (uri.getQuery().isEmpty() ? "" : "&") + addParam.getKey() + "="
                                    + addParam.getValue(),
                            uri.getFragment());
                    requestContext.setUri(uri);
                } catch (URISyntaxException e) {
                    throw new IOError(e);
                }
            }
        }
    }

    @Inject
    TestClock clock;

    /** Jetty server */
    @RegisterExtension
    @Inject
    public JdkServerResource server;

    @RegisterExtension
    public final LogRule log = TestAppender.rule(ApiLoggingFeature.class);

    /** API-internal object */
    @Inject
    LocalGameChatMetrics botInfo;

    private GameChatClientMetrics remoteMetrics = new GameChatClientMetrics();

    @Inject
    GameChatClient gameChatClient;

    /** Endpoint which goes through the started HTTP API */
    private BotStatus botStatus;

    /** Endpoint which goes through the started HTTP API */
    private BeatmapDifficulties beatmapDifficulties;

    private final SetHeaderAndParam clientRequestFilter = new SetHeaderAndParam();

    @BeforeEach
    public void startServer() throws Exception {
        // build clients
        Client client = ClientBuilder.newBuilder().register(clientRequestFilter).build();
        WebTarget target = client.target("http://localhost:" + server.getPort());
        botStatus = WebResourceFactory.newResource(BotStatus.class, target);
        beatmapDifficulties = WebResourceFactory.newResource(BeatmapDifficulties.class, target);
        when(gameChatClient.getMetrics()).thenReturn(ok(remoteMetrics));
    }

    @Test
    public void testIsReceiving() throws Exception {
        clock.advanceBy(60 * 60 * 1000); // one hour
        remoteMetrics.setLastReceivedMessage(60 * 60 * 1000 - 31000); // thirty-one seconds ago
        assertThatThrownBy(() -> botStatus.isReceiving()).isInstanceOf(NotFoundException.class);
        assertThatOurLogs()
                .hasOnlyOneElementSatisfying(mdc("apiPath", "botinfo/isReceiving")
                        .andThen(mdc("apiStatus", "404"))
                        .andThen(mdc("osuApiRateBlockedTime", "0")));
        log.clear();
        remoteMetrics.setLastReceivedMessage(60 * 60 * 1000 - 30000); // thirty seconds ago
        assertTrue(botStatus.isReceiving());
        assertThatOurLogs()
                .hasOnlyOneElementSatisfying(mdc("apiPath", "botinfo/isReceiving")
                        .andThen(mdc("apiStatus", "200"))
                        .andThen(mdc("osuApiRateBlockedTime", "0"))
                        .andThen(mdc("apiKey", null)));
    }

    @Test
    public void testAuthenticationByParam() throws Throwable {
        assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1))
                .isInstanceOf(NotAuthorizedException.class);
        clientRequestFilter.addParam = Pair.of("k", "valid-key");
        beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
        assertThatOurLogs().hasSize(2).element(1).satisfies(mdc("apiKey", "valid-ke")); // truncated
    }

    @Test
    public void testAuthenticationByHeader() throws Throwable {
        assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1))
                .isInstanceOf(NotAuthorizedException.class);
        clientRequestFilter.addToHeader = Pair.of("api-key", "valid-key");
        beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
        assertThatOurLogs().hasSize(2).element(1).satisfies(mdc("apiKey", "valid-ke")); // truncated
    }

    private ListAssert<LogEvent> assertThatOurLogs() {
        return log.assertThat().filteredOn(event -> event.getLoggerName().equals(ApiLoggingFeature.class.getName()));
    }
}
