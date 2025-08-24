package org.tillerino.ppaddict;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.live.LiveContainer.getLive;
import static org.tillerino.ppaddict.util.TestAppender.mdc;
import static tillerino.tillerinobot.MysqlContainer.mysql;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.inject.Singleton;

import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent;
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.MessagePreprocessor;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.impl.RabbitQueuesModule;
import org.tillerino.ppaddict.chat.impl.ResponsePostprocessor;
import org.tillerino.ppaddict.chat.irc.IrcContainer;
import org.tillerino.ppaddict.chat.irc.KittehForNgircd;
import org.tillerino.ppaddict.chat.irc.NgircdContainer;
import org.tillerino.ppaddict.config.CachedDatabaseConfigServiceModule;
import org.tillerino.ppaddict.live.AbstractLiveActivityEndpointTest.GenericWebSocketClient;
import org.tillerino.ppaddict.rabbit.RabbitMqConfiguration;
import org.tillerino.ppaddict.rabbit.RabbitMqContainerConnection;
import org.tillerino.ppaddict.rabbit.RemoteEventQueue;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.rabbitmq.client.Connection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engio.mbassy.listener.Handler;
import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.BotBackend.BeatmapsLoader;
import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.FakeAuthenticationService;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.TestBackend.TestBeatmapsLoader;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.BotInfoService;
import tillerino.tillerinobot.rest.BotStatus;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class FullBotTest {
	@SuppressWarnings({ "unchecked" })
	private class Client implements Runnable {
		private final org.kitteh.irc.client.library.Client kitteh;

		private long lastReceivedRecommendation = 0;

		private int receivedRecommendations = 0;

		private boolean connected = false;

		private int botNumber;

		private Client(int botNumber) {
			kitteh = KittehForNgircd.buildKittehClient("user" + botNumber);
			kitteh.getEventManager().registerEventListener(this);
			this.botNumber = botNumber;
		}

		@Handler
		public void onConnect(ClientConnectionEstablishedEvent event) {
			connected = true;
		}

		@Handler
		public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
			log.debug("user{} received private message from {}: {}", botNumber,
					event.getActor().getNick(), event.getMessage());
			if (event.getMessage().contains(IRCBot.VERSION_MESSAGE)) {
				return;
			}
			if (event.getMessage().contains("Beatmap")) {
				receivedRecommendations++;
				lastReceivedRecommendation = System.currentTimeMillis();
				recommendationCount.incrementAndGet();
			}
			if (receivedRecommendations < recommendationsPerUser) {
				Thread.sleep(10);
				r();
			} else {
				log.debug("user{} received {} recommendations. Quitting.", botNumber,
						recommendationsPerUser);
				kitteh.shutdown();
			}
		}

		@Override
		public void run() {
			try {
				kitteh.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void r() {
			kitteh.sendMessage("tillerinobot", "!r");
		}
	}

	@RequiredArgsConstructor
	protected class FullBotConfiguration extends AbstractModule {
		private final String host;
		private final int port;

		@Override
		protected void configure() {
			install(new ProcessorsModule());
			install(new CachedDatabaseConfigServiceModule());

			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.server")).toInstance(host);
			bind(Integer.class).annotatedWith(Names.named("tillerinobot.irc.port")).toInstance(port);
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.nickname")).toInstance("tillerinobot");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.password")).toInstance("");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.autojoin")).toInstance("#osu");

			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.ignore")).toInstance(false);
			bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
			bind(Recommender.class).to(TestBackend.TestRecommender.class).in(Singleton.class);
			bind(Clock.class).toInstance(Clock.system());
			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend")).toInstance(false);
			bind(BeatmapsLoader.class).to(TestBeatmapsLoader.class);
			install(new MessageHandlerSchedulerModule());
			bind(int.class).annotatedWith(Names.named("coreSize")).toInstance(4);
			bind(AuthenticationService.class).toInstance(new FakeAuthenticationService());
			bind(Connection.class).toInstance(rabbit.getConnection());
			install(new RabbitQueuesModule());
			install(new DockeredMysqlModule());
			bind(BotStatus.class).to(BotInfoService.class);
		}
	}

	static {
		// make sure these are started
		mysql();
		getLive();
	}

	@Mock
	private GenericWebSocketClient client;
	private final WebSocketClient webSocketClient = new WebSocketClient();

	private final ThreadGroup clients = new ThreadGroup("Clients");
	@Rule
	public final ExecutorServiceRule clientExec = new ExecutorServiceRule(() -> new ThreadPoolExecutor(0,
			Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(clients, r, "Client")));

	public final ExecutorServiceRule exec = new ExecutorServiceRule(() -> new ThreadPoolExecutor(0, Integer.MAX_VALUE,
			1L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "aux")));
	public RabbitMqContainerConnection rabbit = new RabbitMqContainerConnection(exec);
	@Rule
	public RuleChain chain = RuleChain.outerRule(exec).around(rabbit);

	private ExecutorService coreWorkerPool;

	@Rule
	public final LogRule logRule = TestAppender.rule(MessagePreprocessor.class, ResponsePostprocessor.class);

	private final AtomicInteger recommendationCount = new AtomicInteger();
	protected final List<Future<?>> started = new ArrayList<>();

	protected int users = 2;
	protected int recommendationsPerUser = 10;

	private BotStatus botInfoApi;

	@Rule
	public MysqlDatabaseLifecycle lifecycle = new MysqlDatabaseLifecycle();

	@Before
	public void startBot() throws Exception {
		Injector injector = Guice.createInjector(new FullBotConfiguration(NgircdContainer.NGIRCD.getHost(), NgircdContainer.NGIRCD.getMappedPort(6667)));

		coreWorkerPool = injector.getInstance(Key.get(ThreadPoolExecutor.class, Names.named("core")));
		webSocketClient.start();
		String wsUrl = "ws://" + getLive().getHost() + ":" + getLive().getMappedPort(8080) + "/live/v0";
		log.info("Connecting to websocket at {}", wsUrl);
		Future<Session> connect = webSocketClient.connect(client, new URI(wsUrl));
		connect.get(10, TimeUnit.SECONDS);

		TestBackend backend = (TestBackend) injector.getInstance(BotBackend.class);
		for (int botNumber = 0; botNumber < users; botNumber++) {
			backend.hintUser("user" + botNumber, false, 12, 1000);
		}
		botInfoApi = injector.getInstance(BotStatus.class);
		RemoteEventQueue externalEventQueue = RabbitMqConfiguration.externalEventQueue(rabbit.getConnection());
		externalEventQueue.setup();
		externalEventQueue.subscribe(injector.getInstance(MessagePreprocessor.class)::onEvent);
		IrcContainer.TILLERINOBOT_IRC.start();
	}

	@After
	public void stopBot() throws Exception {
		started.forEach(fut -> fut.cancel(true));
		if (coreWorkerPool != null) {
			coreWorkerPool.shutdownNow();
		}
		webSocketClient.stop();
	}

	@Test
	public void testMultipleUsers() {
		List<Client> clients = IntStream.range(0, users).mapToObj(Client::new).collect(toList());
		clients.forEach(client -> {
			try {
				// we have to spread out our connection attempts a bit or they might fail
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			clientExec.submit(client);
		});
		clients.forEach(client -> {
			await().until(() -> client.connected);
			client.r();
		});
		int total = users * recommendationsPerUser;
		Callable<Boolean> allRecommendationsReceived = () -> recommendationCount.get() == total;
		for (int i = 0; i < 100; i++) {
			try {
				log.info("Waiting for recommendation count to reach {}. Current: {}.", total,
						recommendationCount.get());
				await().atMost(Duration.ofSeconds(2)).until(allRecommendationsReceived);
			} catch (ConditionTimeoutException e) {
				log.info("Some clients got concurrent messages. Let's give 'em a push.");
				clients.stream().filter(client -> client.receivedRecommendations < recommendationsPerUser)
						.filter(client -> client.lastReceivedRecommendation < System.currentTimeMillis() - 2000)
						.forEach(Client::r);
				continue;
			}
			break;
		}
		await().atMost(Duration.ofSeconds(2)).until(allRecommendationsReceived);

		verify(client, timeout(10000).atLeast(total)).message(argThat(s -> s.contains("\"received\":")));
		verify(client, timeout(1000).atLeast(total)).message(argThat(s -> s.contains("\"sent\":")));
		verify(client, timeout(1000).atLeast(total)).message(argThat(s -> s.contains("\"messageDetails\":")));

		logRule.assertThat()
			.anySatisfy(mdc("handler", "r"))
			.allSatisfy(event -> assertThat(event.getDiagnosticContext()).containsKey("event"))
			.allSatisfy(event -> assertThat(event.getDiagnosticContext()).containsKey("user"));

		assertThat(botInfoApi.isReceiving()).isTrue();
		assertThat(botInfoApi.isSending()).isTrue();
		assertThat(botInfoApi.isRecommending()).isTrue();

		log.info("Received {} recommendations. Quitting.", recommendationCount.get());
	}
}
