package tillerino.tillerinobot.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static tillerino.tillerinobot.websocket.LiveActivityEndpoint.anonymizeHashCode;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveActivityEndpointTest {
	@WebSocket
	public class GenericWebsocketClient {
		private final GenericWebsocketClient delegate;

		public GenericWebsocketClient(GenericWebsocketClient delegate) {
			this.delegate = delegate;
		}

		@OnWebSocketConnect
		public void connect(Session session) {
			LiveActivityEndpointTest.this.session.complete(session);
			delegate.connect(session);
		}

		@OnWebSocketMessage
		public void message(String text) {
			delegate.message(text);
		}

		@OnWebSocketClose
		public void disconnect(int code, String message) {
			delegate.disconnect(code, message);
		}
	}

	@Test
	public void testAnonymize() throws Exception {
		// deterministic
		assertThat(anonymizeHashCode("a", "b")).isEqualTo(anonymizeHashCode("a", "b"));
		// change salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("a", "c"));
		// change object
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("d", "b"));
		// swap object and salt
		assertThat(anonymizeHashCode("a", "b")).isNotEqualTo(anonymizeHashCode("b", "a"));
	}

	@Rule
	public final JettyWebsocketServerResource websocketServer = new JettyWebsocketServerResource("localhost", 0);
	private Future<Session> connect;
	private WebSocketClient webSocketClient = new WebSocketClient();

	@Mock
	private GenericWebsocketClient client;

	private final CompletableFuture<Session> session = new CompletableFuture<>();

	private final LiveActivityEndpoint liveActivity = new LiveActivityEndpoint();

	@Before
	public void setUp() throws Exception {
		websocketServer.addEndpoint(liveActivity);
		webSocketClient.start();
		connect = webSocketClient.connect(new GenericWebsocketClient(client),
				new URI("ws://localhost:" + websocketServer.getPort() + "/live/v0"));
		connect.get(10, TimeUnit.SECONDS);
	}

	@After
	public void tearDown() throws Exception {
		connect.cancel(true);
		webSocketClient.stop();
	}

	@Test
	public void testPing() throws Exception {
		verify(client, timeout(1000)).connect(any());
		session().getRemote().sendString("PING");
		verify(client, timeout(1000)).message("PONG");
	}

	@Test
	public void testPropagateMessageReceived() throws Exception {
		waitForConnectionEstablished();
		liveActivity.propagateReceivedMessage("user", 15);
		verify(client, timeout(1000)).message("{\n" + 
				"  \"received\" : {\n" + 
				"    \"eventId\" : 15,\n" + 
				"    \"user\" : " + LiveActivityEndpoint.anonymizeHashCode("user", liveActivity.getSessions().iterator().next()) + "\n" + 
				"  }\n" + 
				"}");
	}

	@Test
	public void testPropagateMessageSent() throws Exception {
		waitForConnectionEstablished();
		liveActivity.propagateSentMessage("user", 15);
		verify(client, timeout(1000)).message("{\n" + 
				"  \"sent\" : {\n" + 
				"    \"eventId\" : 15,\n" + 
				"    \"user\" : " + LiveActivityEndpoint.anonymizeHashCode("user", liveActivity.getSessions().iterator().next()) + "\n" + 
				"  }\n" + 
				"}");
	}

	@Test
	public void testpropagateMessageDetails() throws Exception {
		waitForConnectionEstablished();
		liveActivity.propagateMessageDetails(15, "!r");
		verify(client, timeout(1000)).message("{\n" + 
				"  \"messageDetails\" : {\n" + 
				"    \"eventId\" : 15,\n" + 
				"    \"message\" : \"!r\"\n" + 
				"  }\n" + 
				"}");
	}

	private Session session() throws Exception {
		return session.get(1, TimeUnit.SECONDS);
	}

	private void waitForConnectionEstablished() {
		verify(client, timeout(1000)).connect(any());
		await().until(() -> !liveActivity.getSessions().isEmpty());
	}
}
