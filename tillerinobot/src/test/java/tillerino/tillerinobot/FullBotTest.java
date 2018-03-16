package tillerino.tillerinobot;

import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;

import java.nio.charset.Charset;
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
import javax.ws.rs.NotFoundException;

import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.pircbotx.Configuration;
import org.pircbotx.Configuration.Builder;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.rest.AuthenticationService.Authorization;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;
import tillerino.tillerinobot.testutil.ExecutorServiceRule;

/**
 * This test starts an embedded IRC server, mocks a backend and requests
 * recommendations from multiple users in parallel.
 */
@Slf4j
public class FullBotTest {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private class Client implements Runnable {
		private PircBotX bot;

		private long lastReceivedRecommendation = 0;

		private int receivedRecommendations = 0;

		private boolean connected = false;

		private Client(int botNumber) {
			Builder<PircBotX> configurationBuilder = new Configuration.Builder<PircBotX>()
					.setServer("127.0.0.1", server.getPort())
					.setName("user" + botNumber)
					.setEncoding(Charset.forName("UTF-8"))
					.setAutoReconnect(false)
					.setMessageDelay(50)
					.setListenerManager(new ThreadedListenerManager<>(exec))
					.addListener(new CoreHooks() {
						@Override
						public void onConnect(ConnectEvent event) throws Exception {
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
			() -> new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));

	private final AtomicInteger recommendationCount = new AtomicInteger();

	private BotRunner botRunner;

	private Future<?> botRunnerFuture;

	class FullBotConfiguration extends AbstractModule {
		@Override
		protected void configure() {
			install(new CreateInMemoryDatabaseModule());
			install(new TillerinobotConfigurationModule());

			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.server")).toInstance("localhost");
			bind(Integer.class).annotatedWith(Names.named("tillerinobot.irc.port")).toInstance(server.getPort());
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.nickname")).toInstance("tillerinobot");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.password")).toInstance("");
			bind(String.class).annotatedWith(Names.named("tillerinobot.irc.autojoin")).toInstance("#osu");

			bind(BotRunner.class).to(BotRunnerImpl.class);
			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.ignore")).toInstance(false);
			bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
			bind(Boolean.class).annotatedWith(Names.named("tillerinobot.test.persistentBackend")).toInstance(false);
			bind(ExecutorService.class).annotatedWith(Names.named("tillerinobot.maintenance")).toInstance(exec);
			bind(AuthenticationService.class).toInstance(key -> {
				if (key.equals("testKey")) {
					return new Authorization(false);
				}
				throw new NotFoundException();
			});
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
		Injector injector = Guice.createInjector(new FullBotConfiguration());
		BotInfo botInfo = injector.getInstance(BotInfo.class);
		TestBackend backend = (TestBackend) injector.getInstance(BotBackend.class);
		botRunner = injector.getInstance(BotRunner.class);
		botRunnerFuture = exec.submit(botRunner);
		for (int botNumber = 0; botNumber < USERS; botNumber++) {
			backend.hintUser("user" + botNumber, false, 12, 1000);
		}
		await().until(() -> botInfo.getLastInteraction() > 0);
	}

	@After
	public void stopBot() {
		botRunnerFuture.cancel(true);
		botRunner.getBot().sendIRC().quitServer();
	}

	@Test
	public void testMultipleUsers() throws Exception {
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
		log.info("Received {} recommendations. Quitting.", recommendationCount.get());
	}
}
