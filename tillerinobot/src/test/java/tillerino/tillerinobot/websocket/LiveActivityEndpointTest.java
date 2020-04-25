package tillerino.tillerinobot.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static tillerino.tillerinobot.websocket.LiveActivityEndpoint.anonymizeHashCode;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveActivityEndpointTest {
	@WebSocket
	public interface GenericWebSocketClient {
		@OnWebSocketConnect
		void connect(Session session);

		@OnWebSocketMessage
		void message(String text);

		@OnWebSocketClose
		void disconnect(int code, String message);
	}

	@Test
	public void testAnonymize() {
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
	public final JettyWebsocketServerResource webSocketServer = new JettyWebsocketServerResource("localhost", 0);
	private final WebSocketClient webSocketClient = new WebSocketClient();

	@Mock
	private GenericWebSocketClient client;

	private final LiveActivityEndpoint liveActivity = new LiveActivityEndpoint();

	@Before
	public void setUp() throws Exception {
		webSocketServer.addEndpoint(liveActivity);
		webSocketClient.start();
		Future<Session> connect = webSocketClient.connect(client,
				new URI("ws://localhost:" + webSocketServer.getPort() + "/live/v0"));
		connect.get(10, TimeUnit.SECONDS);
	}

	@After
	public void tearDown() throws Exception {
		webSocketClient.stop();
	}

	@Test
	public void testPing() throws Exception {
		verify(client, timeout(1000)).connect(any());
		session().getRemote().sendString("PING");
		verify(client, timeout(1000)).message("PONG");
	}

	@Test
	public void testPropagateMessageReceived() {
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
	public void testPropagateMessageSent() {
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
	public void testPropagateMessageDetails() {
		waitForConnectionEstablished();
		liveActivity.propagateMessageDetails(15, "!r");
		verify(client, timeout(1000)).message("{\n" + 
				"  \"messageDetails\" : {\n" + 
				"    \"eventId\" : 15,\n" + 
				"    \"message\" : \"!r\"\n" + 
				"  }\n" + 
				"}");
	}

	private Session session() {
		final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
		verify(client, timeout(1000)).connect(captor.capture());
		return captor.getValue();
	}

	private void waitForConnectionEstablished() {
		verify(client, timeout(1000)).connect(any());
		await().until(() -> !liveActivity.getSessions().isEmpty());
	}
}
