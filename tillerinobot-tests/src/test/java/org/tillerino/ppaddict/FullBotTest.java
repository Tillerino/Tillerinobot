package org.tillerino.ppaddict;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.tillerino.ppaddict.live.LiveContainer.getLive;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.impl.RabbitQueuesModule;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl;
import org.tillerino.ppaddict.chat.irc.NgircdContainer;
import org.tillerino.ppaddict.config.CachedDatabaseConfigServiceModule;
import org.tillerino.ppaddict.live.AbstractLiveActivityEndpointTest.GenericWebSocketClient;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;
import org.tillerino.ppaddict.rabbit.RabbitMqContainerConnection;
import org.tillerino.ppaddict.rabbit.RabbitRpc;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;
import org.tillerino.ppaddict.web.BarePpaddictUserDataService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.rabbitmq.client.Connection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.BotBackend.BeatmapsLoader;
import tillerino.tillerinobot.FakeAuthenticationService;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.TestBackend.TestBeatmapsLoader;
import tillerino.tillerinobot.TillerinobotConfigurationModule;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.BotInfoService;
import tillerino.tillerinobot.rest.BotStatus;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class FullBotTest {
	@SuppressWarnings({ "unchecked" })
	private class Client implements Runnable {
		private final PircBotX bot;

		private long lastReceivedRecommendation = 0;

		private int receivedRecommendations = 0;

		private boolean connected = false;

		private Client(int botNumber) {
			Builder<PircBotX> configurationBuilder = new Builder<>().setServer(NgircdContainer.NGIRCD.getHost(), NgircdContainer.NGIRCD.getMappedPort(6667))
					.setName("user" + botNumber).setEncoding(StandardCharsets.UTF_8).setAutoReconnect(false)
					.setMessageDelay(50).setListenerManager(new ThreadedListenerManager<>(exec))
					.addListener(new CoreHooks() {
						@Override
						public void onConnect(ConnectEvent event) {
							connected = true;
						}

						@Override
						public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
							log.debug("user{} received private message from {}: {}", botNumber,
									event.getUser().getNick(), event.getMessage());
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
								event.getBot().sendIRC().quitServer();
							}
						}
					});
			bot = new PircBotX(configurationBuilder.buildConfiguration());
		}

		@Override
		public void run() {
			try {
				bot.startBot();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void r() {
			bot.sendIRC().message("tillerinobot", "!r");
		}
	}

	@RequiredArgsConstructor
	protected class FullBotConfiguration extends AbstractModule {
		private final String host;
		private final int port;

		@Override
		protected void configure() {
			install(new TillerinobotConfigurationModule());
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
			bind(new TypeLiteral<AbstractPpaddictUserDataService<?>>() {
			}).to(BarePpaddictUserDataService.class);
			bind(Connection.class).toInstance(rabbit.getConnection());
			install(new RabbitQueuesModule());
			install(new CreateInMemoryDatabaseModule() {
				@Override
				protected DataSource dataSource() {
					MysqlDataSource dataSource = new MysqlDataSource();
					dataSource.setURL(MYSQL.getJdbcUrl());
					dataSource.setUser(MYSQL.getUsername());
					dataSource.setPassword(MYSQL.getPassword());
					return dataSource;
				}
			});
			bind(BotStatus.class).to(BotInfoService.class);
		}
	}

	private static final MySQLContainer MYSQL = new MySQLContainer<>();

	static {
		// these take a little longer to start, so we'll do that async
		ForkJoinTask<?> mysql = ForkJoinTask.adapt((Runnable) MYSQL::start).fork();
		RabbitMqContainer.getRabbitMq(); // make sure it's started
		getLive();
		mysql.join();
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
	public final LogRule logRule = TestAppender.rule();

	private final AtomicInteger recommendationCount = new AtomicInteger();
	protected final List<Future<?>> started = new ArrayList<>();

	protected int users = 2;
	protected int recommendationsPerUser = 10;

	protected BotRunnerImpl botRunner;

	private BotStatus botInfoApi;

	@BeforeClass
	public static void setMessageDelay() {
		BotRunnerImpl.MESSAGE_DELAY = 1;
	}

	@AfterClass
	public static void resetMessageDelay() {
		BotRunnerImpl.MESSAGE_DELAY = BotRunnerImpl.DEFAULT_MESSAGE_DELAY;
	}

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
		botRunner = injector.getInstance(BotRunnerImpl.class);
		started.add(exec.submit(botRunner));
		for (int botNumber = 0; botNumber < users; botNumber++) {
			backend.hintUser("user" + botNumber, false, 12, 1000);
		}
		exec.submit(RabbitRpc.handleRemoteCalls(rabbit.getConnection(), GameChatWriter.class, botRunner.getWriter(),
				new GameChatWriter.Error.Unknown())::mainloop);
		exec.submit(RabbitRpc.handleRemoteCalls(rabbit.getConnection(), GameChatClient.class, botRunner,
				new GameChatClient.Error.Unknown())::mainloop);
		botInfoApi = injector.getInstance(BotStatus.class);
		await().until(() -> botRunner.getMetrics().ok().get().getLastInteraction() > 0);
	}

	@After
	public void stopBot() throws Exception {
		started.forEach(fut -> fut.cancel(true));
		if (botRunner != null) {
			botRunner.stopReconnecting();
			botRunner.disconnectSoftly();
		}
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
				.anySatisfy(event -> assertThat(event.getContextData().toMap()).containsEntry("handler", "r"));

		assertThat(botInfoApi.isReceiving()).isTrue();
		assertThat(botInfoApi.isSending()).isTrue();
		assertThat(botInfoApi.isRecommending()).isTrue();

		log.info("Received {} recommendations. Quitting.", recommendationCount.get());
	}
}
