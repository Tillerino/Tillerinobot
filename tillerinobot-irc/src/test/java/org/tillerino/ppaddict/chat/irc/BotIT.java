package org.tillerino.ppaddict.chat.irc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.tillerino.ppaddict.chat.irc.IrcContainer.TILLERINOBOT_IRC;
import static org.tillerino.ppaddict.chat.irc.NgircdContainer.NGIRCD;
import static org.tillerino.ppaddict.rabbit.RabbitMqContainer.RABBIT_MQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.engio.mbassy.listener.Handler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.helper.ClientEvent;
import org.kitteh.irc.client.library.event.user.PrivateCtcpQueryEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.mockito.Mockito;
import org.tillerino.ppaddict.chat.*;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitRpc;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;

import com.rabbitmq.client.Connection;

import io.restassured.RestAssured;

public class BotIT {
	@Rule
	public final TestName testName = new TestName();

	private Connection connection;
	private final List<GameChatEvent> incoming = Collections.synchronizedList(new ArrayList<>());
	private Client kitteh;
	private GameChatWriter writer;
	private GameChatClient gameChatClient;
	private KittehListener listener = mock(KittehListener.class);
	private List<ClientEvent> kittehEvents = Collections.synchronizedList(new ArrayList<>());

	@Before
	public void setUp() throws Exception {
		System.out.println("Running " + testName.getMethodName());
		RABBIT_MQ.start();
		NGIRCD.start();
		startBot();

		await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));

		kitteh = Client.builder()
			.nick("test")
			.server()
				.host(NGIRCD.getHost())
				.port(NGIRCD.getMappedPort(6667), SecurityType.INSECURE)
			.then().listeners()
			.exception(e -> {
				if (e instanceof KittehNagException) {
					return;
				}
				e.printStackTrace();
			})
			.then().build();
		kitteh.getEventManager().registerEventListener(listener);
		doAnswer(invocation -> {
			ClientEvent event = invocation.getArgument(0);
			if (!(event instanceof ClientReceiveCommandEvent)) {
				// all messages are somehow double by this kind of event
				kittehEvents.add(event);
			}
			return null;
		}).when(listener).onMessage(Mockito.any(ClientEvent.class));

		connection = RabbitMqConfiguration.connectionFactory(RABBIT_MQ.getHost(), RABBIT_MQ.getAmqpPort())
			.newConnection("test");
		RemoteEventQueue incomingQueue = RabbitMqConfiguration.externalEventQueue(connection);
		incomingQueue.setup();
		incomingQueue.subscribe(incoming::add);
		writer = RabbitRpc.remoteCallProxy(connection, GameChatWriter.class, new GameChatWriter.Error.Unknown());
		gameChatClient = RabbitRpc.remoteCallProxy(connection, GameChatClient.class, new GameChatClient.Error.Unknown());
	}

	protected void startBot() throws Exception {
		TILLERINOBOT_IRC.start();
		RestAssured.baseURI = "http://" + TILLERINOBOT_IRC.getHost() + ":"
				+ TILLERINOBOT_IRC.getMappedPort(8080) + "/";
	}

	protected void stopBot() throws Exception {
		TILLERINOBOT_IRC.stop();
	}

	protected void connectKitteh() {
		kitteh.connect();
		await().untilAsserted(() -> assertThat(kittehEvents).anySatisfy(event ->
			assertThat(event).isInstanceOfSatisfying(ClientReceiveNumericEvent.class, message -> {
			assertThat(message.getNumeric()).isEqualTo(318); // end of WHOIS
		})));
		kittehEvents.clear();
	}

	@After
	public void tearDown() throws Exception {
		if (connection != null && connection.isOpen()) {
			connection.close();
		}
		kitteh.shutdown();
	}

	@Test
	public void livenessReactsToRabbit() throws Exception {
		RABBIT_MQ.stop();
		await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
		RABBIT_MQ.start();
		await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
	}

	@Test
	public void livenessReactsToNgircd() throws Exception {
		NGIRCD.stop();
		await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(503));
		NGIRCD.start();
		await().untilAsserted(() -> RestAssured.when().get("/live").then().statusCode(200));
	}

	@Test
	public void incomingPrivateMessage() throws Exception {
		connectKitteh();
		kitteh.sendMessage("tillerinobot", "hello");

		await().untilAsserted(() -> assertThat(incoming)
			.singleElement()
			.isInstanceOfSatisfying(PrivateMessage.class, message -> {
				assertThat(message.getNick()).isEqualTo("test");
				assertThat(message.getMessage()).isEqualTo("hello");
			}));
	}

	@Test
	public void incomingPrivateAction() throws Exception {
		connectKitteh();
		kitteh.sendMessage("tillerinobot", "\u0001ACTION hello\u0001");
 
		await().untilAsserted(() -> assertThat(incoming)
			.singleElement()
			.isInstanceOfSatisfying(PrivateAction.class, message -> {
				assertThat(message.getNick()).isEqualTo("test");
				assertThat(message.getAction()).isEqualTo("hello");
			}));
	}

	@Test
	public void joiningChannelResultsInJoinedEvent() throws Exception {
		connectKitteh();
		kitteh.addChannel("#osu");
		await().untilAsserted(() -> assertThat(incoming)
			.singleElement()
			.isInstanceOfSatisfying(Joined.class, message -> {
				assertThat(message.getNick()).isEqualTo("test");
			}));
	}

	@Test
	public void startingTillerinobotAfterJoiningServerResultsInSightedEvent() throws Exception {
		stopBot();
		NGIRCD.logs.clear();
		connectKitteh();
		kitteh.addChannel("#osu");
		await().untilAsserted(() -> assertThat(NGIRCD.logs).anySatisfy(message -> {
			assertThat(message).contains("Kitteh");
		}));
		startBot();
		await().untilAsserted(() -> assertThat(incoming)
			.singleElement()
			.isInstanceOfSatisfying(Sighted.class, message -> {
				assertThat(message.getNick()).isEqualTo("test");
				assertThat(message.getMeta()).isNotNull().satisfies(meta ->
						assertThat(meta.getMdc().mdcValues()).containsKey("event"));
			}));
	}

	@Test
	public void outgoingPrivateMessage() throws Exception {
		connectKitteh();
		writer.message("hello", "test");
		await().untilAsserted(() -> assertThat(kittehEvents)
			.singleElement()
			.isInstanceOfSatisfying(PrivateMessageEvent.class, message -> {
				assertThat(message.getTarget()).isEqualTo("test");
				assertThat(message.getMessage()).isEqualTo("hello");
			}));
	}

	@Test
	public void outgoingPrivateAction() throws Exception {
		connectKitteh();
		writer.action("hello", "test");
		await().untilAsserted(() -> assertThat(kittehEvents)
			.singleElement()
			.isInstanceOfSatisfying(PrivateCtcpQueryEvent.class, message -> {
				assertThat(message.getTarget()).isEqualTo("test");
				assertThat(message.getCommand()).isEqualTo("ACTION");
				assertThat(message.getMessage()).isEqualTo("ACTION hello");
			}));
	}

	interface KittehListener {
		@Handler
		void onMessage(ClientEvent event);
	}
}
