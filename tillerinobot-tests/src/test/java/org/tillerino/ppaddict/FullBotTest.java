package org.tillerino.ppaddict;

import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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

import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.irc.BotRunnerImpl;
import org.tillerino.ppaddict.chat.irc.EmbeddedIrcServerRule;
import org.tillerino.ppaddict.chat.irc.IrcWriter;
import org.tillerino.ppaddict.chat.local.InMemoryQueuesModule;
import org.tillerino.ppaddict.chat.local.LocalGameChatEventQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.chat.local.LocalGameChatResponseQueue;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.LocalConsoleTillerinobot.FakeAuthenticationService;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.TillerinobotConfigurationModule;
import tillerino.tillerinobot.testutil.ExecutorServiceRule;
import tillerino.tillerinobot.websocket.JettyWebsocketServerResource;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;
import tillerino.tillerinobot.websocket.LiveActivityEndpointTest;

/**
 * This test starts an embedded IRC server, mocks a backend and requests
 * recommendations from multiple users in parallel.
 */
@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class FullBotTest {
	@SuppressWarnings({ "unchecked"})
	private class Client implements Runnable {
		private final PircBotX bot;

		private long lastReceivedRecommendation = 0;

		private int receivedRecommendations = 0;

		private boolean connected = false;

		private Client(int botNumber) {
			Builder<PircBotX> configurationBuilder = new Configuration.Builder<>()
					.setServer("127.0.0.1", server.getPort())
					.setName("user" + botNumber)
					.setEncoding(StandardCharsets.UTF_8)
					.setAutoReconnect(false)
					.setMessageDelay(50)
					.setListenerManager(new ThreadedListenerManager<>(exec))
					.addListener(new CoreHooks() {
						@Override
						public void onConnect(ConnectEvent event) {
							connected = true;
						}

						@Override
						public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
							log.debug("user{} received private message from {}: {}", botNumber, event.getUser().getNick(),
									event.getMessage());
							if (event.getMessage().contains(IRCBot.VERSION_MESSAGE)) {
								return;
							}
							if (event.getMessage().contains("Beatmap")) {
								receivedRecommendations++;
								lastReceivedRecommendation = System.currentTimeMillis();
								recommendationCount.incrementAndGet();
							}
							if (receivedRecommendations < RECOMMENDATIONS_PER_USER) {
								Thread.sleep(10);
								r();
							} else {
								log.debug("user{} received {} recommendations. Quitting.", botNumber, RECOMMENDATIONS_PER_USER);
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

	private static final int USERS = 100;

	private static final int RECOMMENDATIONS_PER_USER = 20;

	@Rule
	public final EmbeddedIrcServerRule server = new EmbeddedIrcServerRule();

	@Rule
	public final ExecutorServiceRule exec = new ExecutorServiceRule(
			() -> new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>()));

	@Rule
	public final ExecutorServiceRule coreWorkerPool = ExecutorServiceRule.fixedThreadPool("core", 4);

	@Rule
	public final JettyWebsocketServerResource webSocket = new JettyWebsocketServerResource("localhost", 0);

	private final WebSocketClient webSocketClient = new WebSocketClient();

	@Mock
	private LiveActivityEndpointTest.GenericWebSocketClient client;

	private final AtomicInteger recommendationCount = new AtomicInteger();

	private GameChatClient botRunner;

	private final List<Future<?>> started = new ArrayList<>();

	@RequiredArgsConstructor
	static class FullBotConfiguration extends AbstractModule {
		private final int port;
		private final ExecutorService maintenanceWorkerPool;
		private final ExecutorService coreWorkerPool;

		@Override
		protected void configure() {
			install(new CreateInMemoryDatabaseModule());
			install(new TillerinobotConfigurationModule());
			install(new InMemoryQueuesModule());
			install(new ProcessorsModule());

			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.server")).toInstance("localhost");
			bind(Integer.class).annotatedWith(Names.named("tillerinobot.irc.port")).toInstance(port);
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.nickname")).toInstance("tillerinobot");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.password")).toInstance("");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.autojoin")).toInstance("#osu");

			bind(GameChatClient.class).to(BotRunnerImpl.class);
			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.ignore")).toInstance(false);
			bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
			bind(GameChatWriter.class).to(IrcWriter.class);
			bind(Clock.class).toInstance(Clock.system());
			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend")).toInstance(false);
			bind(ExecutorService.class).annotatedWith(Names.named("tillerinobot.maintenance")).toInstance(maintenanceWorkerPool);
			bind(ExecutorService.class).annotatedWith(Names.named("core")).toInstance(coreWorkerPool);
			bind(AuthenticationService.class).toInstance(new FakeAuthenticationService());
		}
	}

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
		Injector injector = Guice.createInjector(new FullBotConfiguration(server.getPort(), exec, coreWorkerPool));

		webSocket.addEndpoint(injector.getInstance(LiveActivityEndpoint.class));
		webSocketClient.start();
		Future<Session> connect = webSocketClient.connect(client,
				new URI("ws://localhost:" + webSocket.getPort() + "/live/v0"));
		connect.get(10, TimeUnit.SECONDS);

		LocalGameChatMetrics botInfo = injector.getInstance(LocalGameChatMetrics.class);
		TestBackend backend = (TestBackend) injector.getInstance(BotBackend.class);
		botRunner = injector.getInstance(GameChatClient.class);
		started.add(exec.submit(botRunner));
		started.add(exec.submit(injector.getInstance(LocalGameChatEventQueue.class)));
		started.add(exec.submit(injector.getInstance(LocalGameChatResponseQueue.class)));
		for (int botNumber = 0; botNumber < USERS; botNumber++) {
			backend.hintUser("user" + botNumber, false, 12, 1000);
		}
		await().until(() -> botInfo.getLastInteraction() > 0);
	}

	@After
	public void stopBot() throws Exception {
		started.forEach(fut -> fut.cancel(true));
		botRunner.disconnectSoftly();
		webSocketClient.stop();
	}

	@Test
	public void testMultipleUsers() {
		List<Client> clients = IntStream.range(0, USERS).mapToObj(Client::new).collect(toList());
		clients.forEach(client -> {
			try {
				// we have to spread out our connection attempts a bit or they might fail
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			exec.submit(client);
		});
		clients.forEach(client -> {
			await().until(() -> client.connected);
			client.r();
		});
		Callable<Boolean> allRecommendationsReceived = () -> {
			log.debug("Received {} recommendations so far.", recommendationCount.get());
			return recommendationCount.get() == USERS * RECOMMENDATIONS_PER_USER;
		};
		for (int i = 0; i < 100; i++) {
			try {
				log.info("Waiting for recommendation count to reach {}.", USERS * RECOMMENDATIONS_PER_USER);
				await().atMost(Duration.TEN_SECONDS).until(allRecommendationsReceived);
			} catch (ConditionTimeoutException e) {
				log.info("Some clients got concurrent messages. Let's give 'em a push.");
				clients.stream()
						.filter(client -> client.receivedRecommendations < RECOMMENDATIONS_PER_USER)
						.filter(client -> client.lastReceivedRecommendation < System.currentTimeMillis() - 1000)
						.forEach(Client::r);
				continue;
			}
			break;
		}
		await().atMost(Duration.ONE_SECOND).until(allRecommendationsReceived);
		verify(client, timeout(1000).atLeast(2000)).message(argThat(s -> s.contains("\"received\" :")));
		verify(client, timeout(1000).atLeast(2000)).message(argThat(s -> s.contains("\"sent\" :")));
		verify(client, timeout(1000).atLeast(2000)).message(argThat(s -> s.contains("\"messageDetails\" :")));
		log.info("Received {} recommendations. Quitting.", recommendationCount.get());
	}
}
