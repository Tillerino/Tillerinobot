package tillerino.tillerinobot.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map.Entry;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.rest.AuthenticationService.Authorization;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.data.util.RepositoryModule;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.BotRunner;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;

/**
 * Tests the Tillerinobot API on a live HTTP server including authentication.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiTest {
	class ApiTestModule extends AbstractModule {
		@Override
		protected void configure() {
			install(new CreateInMemoryDatabaseModule());
			install(new RepositoryModule());
			bind(BotBackend.class).toInstance(new TestBackend(false));
			bind(BotRunner.class).toInstance(mock(BotRunner.class));
			bind(BeatmapsService.class).toInstance(mock(BeatmapsService.class));
			// since the authentication service is not mocked yet, we inject a proxy
			bind(AuthenticationService.class).toInstance(key -> authenticationService.findKey(key));
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

	private final Injector injector = Guice.createInjector(new ApiTestModule());

	/**
	 * Jetty server
	 */
	@Rule
	public JettyServerResource server = new JettyServerResource(injector.getInstance(BotApiDefinition.class), "localhost", 0);

	@Mock
	private AuthenticationService authenticationService;

	/**
	 * API-internal object
	 */
	private BotInfo botInfo = injector.getInstance(BotInfo.class);
	
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
	}

	@Test
	public void testBotInfo() throws Exception {
		assertThatThrownBy(() -> botStatus.isReceiving()).isInstanceOf(NotFoundException.class);
		botInfo.setLastReceivedMessage(Long.MAX_VALUE);
		assertTrue(botStatus.isReceiving());
	}

	@Test
	public void testAuthenticationByParam() throws Throwable {
		when(authenticationService.findKey("valid-key")).thenReturn(new Authorization());
		assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1)).isInstanceOf(NotAuthorizedException.class);
		clientRequestFilter.addParam = Pair.of("k", "valid-key");
		beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
	}

	@Test
	public void testAuthenticationByHeader() throws Throwable {
		when(authenticationService.findKey("valid-key")).thenReturn(new Authorization());
		assertThatThrownBy(() -> beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1)).isInstanceOf(NotAuthorizedException.class);
		clientRequestFilter.addToHeader = Pair.of("api-key", "valid-key");
		beatmapDifficulties.getBeatmapInfo(1, 0L, Collections.emptyList(), -1);
	}
}
