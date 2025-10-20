package org.tillerino.ppaddict.live;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.tillerino.ppaddict.chat.LiveActivity;

public abstract class AbstractLiveActivityEndpointTest {
    @WebSocket
    public interface GenericWebSocketClient {
        @OnWebSocketConnect
        void connect(Session session);

        @OnWebSocketMessage
        void message(String text);

        @OnWebSocketClose
        void disconnect(int code, String message);
    }

    private final WebSocketClient webSocketClient = new WebSocketClient();

    private GenericWebSocketClient client = Mockito.mock(GenericWebSocketClient.class);

    protected abstract int port();

    protected abstract String host();

    protected abstract LiveActivity push();

    public void setUp() throws Exception {
        webSocketClient.start();
        Future<Session> connect =
                webSocketClient.connect(client, new URI("ws://" + host() + ":" + port() + "/live/v0"));
        connect.get(10, TimeUnit.SECONDS);
        waitForConnectionEstablished();
    }

    @AfterEach
    public void tearDown() throws Exception {
        webSocketClient.stop();
    }

    @Test
    public void testPing() throws Exception {
        Mockito.verify(client, Mockito.timeout(1000)).connect(ArgumentMatchers.any());
        session().getRemote().sendString("PING");
        Mockito.verify(client, Mockito.timeout(1000)).message("PONG");
    }

    @Test
    public void testPropagateMessageReceived() {
        push().propagateReceivedMessage("user", 15);
        Mockito.verify(client, Mockito.timeout(1000))
                .message(Mockito.argThat(s -> s.matches(
                        "\\{" + "\"received\":\\{" + "\"eventId\":15," + "\"user\":(-?\\d+)" + "\\}" + "\\}")));
    }

    @Test
    public void testPropagateMessageSent() {
        push().propagateSentMessage("user", 15, null);
        Mockito.verify(client, Mockito.timeout(1000))
                .message(Mockito.argThat(s ->
                        s.matches("\\{" + "\"sent\":\\{" + "\"eventId\":15," + "\"user\":(-?\\d+)" + "\\}" + "\\}")));
    }

    @Test
    public void testPropagateMessageSentWithPing() {
        push().propagateSentMessage("user", 15, 12345L);
        Mockito.verify(client, Mockito.timeout(1000))
                .message(Mockito.argThat(s -> s.matches("\\{" + "\"sent\":\\{"
                        + "\"eventId\":15,"
                        + "\"user\":(-?\\d+),"
                        + "\"ping\":12345"
                        + "\\}"
                        + "\\}")));
    }

    @Test
    public void testPropagateMessageDetails() {
        push().propagateMessageDetails(15, "!r");
        Mockito.verify(client, Mockito.timeout(1000))
                .message("{" + "\"messageDetails\":{" + "\"eventId\":15," + "\"message\":\"!r\"" + "}" + "}");
    }

    private Session session() {
        final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        Mockito.verify(client, Mockito.timeout(1000)).connect(captor.capture());
        return captor.getValue();
    }

    private void waitForConnectionEstablished() {
        Mockito.verify(client, Mockito.timeout(1000)).connect(ArgumentMatchers.any());
    }
}
