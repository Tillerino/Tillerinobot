package org.tillerino.ppaddict.chat.irc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.tillerino.ppaddict.chat.irc.IrcContainer.TILLERINOBOT_IRC;
import static org.tillerino.ppaddict.chat.irc.NgircdContainer.NGIRCD;

import com.rabbitmq.client.Connection;
import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.engio.mbassy.listener.Handler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.mockito.Mockito;
import org.tillerino.ppaddict.chat.*;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;
import org.tillerino.ppaddict.rabbit.RabbitRpc;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.util.Result;

public class BotIT {
    private Connection connection;
    private final List<GameChatEvent> incoming = Collections.synchronizedList(new ArrayList<>());
    private Client kitteh;
    private GameChatWriter writer;
    private GameChatClient gameChatClient;
    private final KittehListener listener = mock(KittehListener.class);
    private final List<ClientEvent> kittehEvents = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        RabbitMqContainer.start();
        connection = RabbitMqConfiguration.connectionFactory(
                        RabbitMqContainer.getHost(),
                        RabbitMqContainer.getAmqpPort(),
                        RabbitMqContainer.getVirtualHost())
                .newConnection("test");
        writer = RabbitRpc.remoteCallProxy(connection, GameChatWriter.class, new GameChatWriter.Error.Timeout());
        gameChatClient =
                RabbitRpc.remoteCallProxy(connection, GameChatClient.class, new GameChatClient.Error.Timeout());
        RemoteEventQueue incomingQueue = RabbitMqConfiguration.externalEventQueue(connection);
        incomingQueue.setup();
        incomingQueue.subscribe(incoming::add);

        NGIRCD.start();
        startBot();

        await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
        await().untilAsserted(() -> assertThat(gameChatClient.getMetrics().ok())
                .hasValueSatisfying(metrics -> assertThat(metrics.isConnected()).isTrue()));

        kitteh = KittehForNgircd.buildKittehClient("test");
        kitteh.getEventManager().registerEventListener(listener);
        doAnswer(invocation -> {
                    ClientEvent event = invocation.getArgument(0);
                    if (!(event instanceof ClientReceiveCommandEvent)) {
                        // all messages are somehow double by this kind of event
                        withEvents(events -> events.add(event));
                    }
                    return null;
                })
                .when(listener)
                .onMessage(Mockito.any(ClientEvent.class));
    }

    protected void startBot() {
        TILLERINOBOT_IRC.start();
        RestAssured.baseURI = "http://" + TILLERINOBOT_IRC.getHost() + ":" + TILLERINOBOT_IRC.getMappedPort(8080) + "/";
    }

    protected void stopBot() {
        TILLERINOBOT_IRC.stop();
    }

    protected void connectKitteh() {
        kitteh.connect();
        await().untilAsserted(() -> withEvents(events -> assertThat(events).anySatisfy(event -> assertThat(event)
                .isInstanceOfSatisfying(ClientReceiveNumericEvent.class, message -> {
                    assertThat(message.getNumeric()).isEqualTo(318); // end of WHOIS
                }))));
        withEvents(List::clear);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (kitteh != null) {
            kitteh.shutdown();
        }
    }

    void withEvents(Consumer<List<ClientEvent>> consumer) {
        synchronized (kittehEvents) {
            // since we iterate over the list, a simple synchronizedList is not sufficient
            consumer.accept(kittehEvents);
        }
    }

    @Test
    // This test kills the shared rabbit MQ container, which messes with parallel tests.
    @Disabled
    public void livenessReactsToRabbit() {
        RabbitMqContainer.stop();
        await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
        RabbitMqContainer.start();
        await().atMost(1, TimeUnit.MINUTES)
                .untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
    }

    @Test
    public void livenessReactsToNgircd() {
        NGIRCD.stop();
        await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
        Result<GameChatClientMetrics, GameChatClient.Error> metrics1 = gameChatClient.getMetrics();
        assertThat(metrics1.ok())
                .hasValueSatisfying(metrics -> assertThat(metrics.isConnected()).isFalse());
        NGIRCD.start();
        await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
        assertThat(gameChatClient.getMetrics().ok())
                .hasValueSatisfying(metrics -> assertThat(metrics.isConnected()).isTrue());
    }

    @Test
    public void incomingPrivateMessage() {
        connectKitteh();
        kitteh.sendMessage("tbot", "hello");

        await().untilAsserted(() -> assertThat(incoming)
                .singleElement()
                .isInstanceOfSatisfying(PrivateMessage.class, message -> {
                    assertThat(message.getNick()).isEqualTo("test");
                    assertThat(message.getMessage()).isEqualTo("hello");
                }));
    }

    @Test
    public void incomingPrivateAction() {
        connectKitteh();
        kitteh.sendMessage("tbot", "\u0001ACTION hello\u0001");

        await().untilAsserted(() -> assertThat(incoming)
                .singleElement()
                .isInstanceOfSatisfying(PrivateAction.class, message -> {
                    assertThat(message.getNick()).isEqualTo("test");
                    assertThat(message.getAction()).isEqualTo("hello");
                }));
    }

    @Test
    public void joiningChannelResultsInJoinedEvent() {
        connectKitteh();
        kitteh.addChannel("#osu");
        await().untilAsserted(() -> assertThat(incoming)
                .singleElement()
                .isInstanceOfSatisfying(
                        Joined.class, message -> assertThat(message.getNick()).isEqualTo("test")));
    }

    @Test
    public void startingTillerinobotAfterJoiningServerResultsInSightedEvent() throws Exception {
        stopBot();
        NGIRCD.logs.clear();
        connectKitteh();
        kitteh.addChannel("#osu");
        await().untilAsserted(() -> assertThat(NGIRCD.logs)
                .anySatisfy(message -> assertThat(message).contains("Kitteh")));
        Thread.sleep(1000); // ngIRCd takes a bit to process the join
        startBot();
        await().untilAsserted(
                        () -> assertThat(incoming).singleElement().isInstanceOfSatisfying(Sighted.class, message -> {
                            assertThat(message.getNick()).isEqualTo("test");
                            assertThat(message.getMeta()).isNotNull().satisfies(meta -> assertThat(meta.getMdc())
                                    .isNull());
                        }));
    }

    @Test
    public void outgoingPrivateMessage() {
        connectKitteh();
        assertThat(writer.message("hello", "test").ok()).isPresent();
        await().untilAsserted(() -> withEvents(events -> assertThat(events)
                .singleElement()
                .isInstanceOfSatisfying(PrivateMessageEvent.class, message -> {
                    assertThat(message.getTarget()).isEqualTo("test");
                    assertThat(message.getMessage()).isEqualTo("hello");
                })));
    }

    @Test
    public void outgoingPrivateAction() {
        connectKitteh();
        assertThat(writer.action("hello", "test").ok()).isPresent();
        await().untilAsserted(() -> withEvents(events -> assertThat(events)
                .singleElement()
                .isInstanceOfSatisfying(PrivateCtcpQueryEvent.class, message -> {
                    assertThat(message.getTarget()).isEqualTo("test");
                    assertThat(message.getCommand()).isEqualTo("ACTION");
                    assertThat(message.getMessage()).isEqualTo("ACTION hello");
                })));
    }

    interface KittehListener {
        @Handler
        void onMessage(ClientEvent event);
    }
}
