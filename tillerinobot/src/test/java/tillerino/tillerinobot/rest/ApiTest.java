package tillerino.tillerinobot.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tillerino.ppaddict.util.Result.ok;
import static org.tillerino.ppaddict.util.TestAppender.mdc;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map.Entry;

import javax.persistence.EntityManagerFactory;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.ListAssert;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.Result;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogEventWithMdc;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.util.TestClock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.FakeAuthenticationService;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.TestBackend.TestBeatmapsLoader;
/**
 * Tests the Tillerinobot API on a live HTTP server including authentication.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiTest {
	class ApiTestModule extends AbstractModule {
		@Override
		protected void configure() {
			install(new CreateInMemoryDatabaseModule());
			bind(boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend")).toInstance(false);
			bind(BotBackend.BeatmapsLoader.class).to(TestBeatmapsLoader.class);
			bind(BotBackend.class).to(TestBackend.class);
			bind(GameChatClient.class).toInstance(mock(GameChatClient.class));
			bind(BeatmapsService.class).toInstance(mock(BeatmapsService.class));
			bind(AuthenticationService.class).toInstance(new FakeAuthenticationService());
			bind(Clock.class).toInstance(clock);
		}
	}

	/**
	 * Filter for the client to add a header or query parameter to the request.
	 */
	static class SetHeaderAndParam implements ClientRequestFilter {
		/**
		 * Set this field to add a header to the request
		 */
		private Entry<String, String> addToHeader = null;

		/**
		 * Set this field to add a param to the request
		 */
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
							uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery()
									+ (uri.getQuery().isEmpty() ? "" : "&") + addParam.getKey() + "=" + addParam.getValue(),
							uri.getFragment());
					requestContext.setUri(uri);
				} catch (URISyntaxException e) {
					throw new IOError(e);
				}
			}
		}
	}

	private final TestClock clock = new TestClock();

	private final Injector injector = Guice.createInjector(new ApiTestModule());

	/**
	 * Jetty server
	 */
	@Rule
	public JdkServerResource server = new JdkServerResource(injector.getInstance(BotApiDefinition.class), "localhost", 0);

	@Rule
	public final LogRule log = TestAppender.rule();

	/**
	 * API-internal object
	 */
	private LocalGameChatMetrics botInfo = injector.getInstance(LocalGameChatMetrics.class);
	private GameChatClientMetrics remoteMetrics = new GameChatClientMetrics();

	/**
	 * Endpoint which goes through the started HTTP API
	 */
	private BotStatus botStatus;

	/**
	 * Endpoint which goes through the started HTTP API
	 */
	private BeatmapDifficulties beatmapDifficulties;

	private final SetHeaderAndParam clientRequestFilter = new SetHeaderAndParam();

	@Before
	public void startServer() throws Exception {
		// build clients
		Client client = ClientBuilder.newBuilder().register(clientRequestFilter).build();
		WebTarget target = client.target("http://localhost:" + server.getPort());
		botStatus = WebResourceFactory.newResource(BotStatus.class, target);
		beatmapDifficulties = WebResourceFactory.newResource(BeatmapDifficulties.class, target);
		when(injector.getInstance(GameChatClient.class).getMetrics()).thenReturn(ok(remoteMetrics));
	}

	@After
	public void stop() throws Exception {
		injector.getInstance(EntityManagerFactory.class).close();
	}

	@Test
	public void testIsReceiving() throws Exception {
		clock.advanceBy(60 * 60 * 1000); // one hour
		remoteMetrics.setLastReceivedMessage(60 * 60 * 1000 - 11000); // eleven seconds ago
		assertThatThrownBy(() -> botStatus.isReceiving()).isInstanceOf(NotFoundException.class);
		assertThatOurLogs().hasOnlyOneElementSatisfying(
			mdc("apiPath", "botinfo/isReceiving")
				.andThen(mdc("apiStatus", "404"))
				.andThen(mdc("osuApiRateBlockedTime", "0")));
		log.clear();
		remoteMetrics.setLastReceivedMessage(60 * 60 * 1000 - 10000); // ten seconds ago
		assertTrue(botStatus.isReceiving());
		assertThatOurLogs().hasOnlyOneElementSatisfying(
				mdc("apiPath", "botinfo/isReceiving")
					.andThen(mdc("apiStatus", "200"))
					.andThen(mdc("osuApiRateBlockedTime", "0"))
					.andThen(mdc("apiKey", null)));
	}

	@Test
	public void testAuthenticationByParam() throws Throwable {
		assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1)).isInstanceOf(NotAuthorizedException.class);
		clientRequestFilter.addParam = Pair.of("k", "valid-key");
		beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
		assertThatOurLogs()
			.hasSize(2)
			.element(1).satisfies(mdc("apiKey", "valid-ke")); // truncated
	}

	@Test
	public void testAuthenticationByHeader() throws Throwable {
		assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1)).isInstanceOf(NotAuthorizedException.class);
		clientRequestFilter.addToHeader = Pair.of("api-key", "valid-key");
		beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
		assertThatOurLogs()
			.hasSize(2)
			.element(1).satisfies(mdc("apiKey", "valid-ke")); // truncated
	}

	private ListAssert<LogEventWithMdc> assertThatOurLogs() {
		return log.assertThat()
			.filteredOn(event -> event.getLoggerName().equals(ApiLoggingFeature.class.getName()));
	}
}
