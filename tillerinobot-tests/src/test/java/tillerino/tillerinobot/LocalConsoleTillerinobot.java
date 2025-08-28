package tillerino.tillerinobot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tillerino.ppaddict.util.Result.ok;

import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Component;
import dagger.Provides;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.local.InMemoryQueuesModule;
import org.tillerino.ppaddict.chat.local.LocalGameChatEventQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatResponseQueue;
import org.tillerino.ppaddict.config.CachedDatabaseConfigServiceModule;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.Result;

import com.sun.net.httpserver.HttpServer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;
import tillerino.tillerinobot.BotBackend.BeatmapsLoader;
import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.rest.BeatmapResource;
import tillerino.tillerinobot.rest.BeatmapsService;
import tillerino.tillerinobot.rest.BotApiDefinition;

/**
 * The purpose of this class and its main function is to completely mock backend
 * and IRC connection to quickly check out the functionality of Tillerinobot.
 * 
 * @author Tillerino
 * 
 */
@Slf4j
public class LocalConsoleTillerinobot {
	@Component(modules = {Module.class, ClockModule.class})
	@Singleton
	interface Injector {
		BotApiDefinition botApiDefinition();

		LocalGameChatEventQueue localGameChatEventQueue();

		LocalGameChatResponseQueue localGameChatResponseQueue();

		ConsoleRunner consoleRunner();

		BotBackend botBackend();

		GameChatWriter gameChatWriter();

		@Named("messagePreprocessor") GameChatEventConsumer messagePreprocessor();
	}

	@dagger.Module
	public static class ClockModule {
		private final Clock clock;

    ClockModule(Clock clock) {this.clock = clock;}

		@Provides
		Clock clock() {
			return clock;
		}
  }

	@dagger.Module(includes = {DockeredMysqlModule.class, InMemoryQueuesModule.class, LiveActivityMockModule.class,
														 TestBackend.Module.class, MessageHandlerSchedulerModule.class, ProcessorsModule.class,
														 TestOsutrackDownloader.Module.class, OsuApiV1.Module.class, OsuApiV1Test.Module.class,
														 CachedDatabaseConfigServiceModule.class})

	public interface Module {
		@dagger.Provides
		static @Named("tillerinobot.test.persistentBackend") boolean persistentBackend() {
			return false;
		}

		@dagger.Provides
		static @Named("coreSize") int coreSize() {
			return 1;
		}

		@Binds
		AuthenticationService authenticationService(FakeAuthenticationService fakeAuthenticationService);

		@Binds
		GameChatClient gameChatClient(ConsoleRunner consoleRunner);

		@dagger.Provides
		@Singleton
		static BeatmapsService getBeatmapsService(BeatmapsLoader backend) {
			BeatmapsService beatService = mock(BeatmapsService.class);
			when(beatService.byId(anyInt())).thenAnswer(req -> {
				BeatmapResource res = mock(BeatmapResource.class);
				doAnswer(x -> backend.getBeatmap((Integer) req.getArguments()[0], 0L)).when(res).get();
				doAnswer(x -> { System.out.println("Beatmap uploaded: " + x.getArguments()[0]); return null; }).when(res).setFile(anyString());
				return res;
			});
			return beatService;
		}

		@dagger.Provides
		@Singleton
		@SneakyThrows
		static GameChatWriter writer() {
			GameChatWriter writer = mock(GameChatWriter.class);
			doAnswer(invocation -> {
				System.out.println("*Tillerino " + invocation.getArguments()[0]);
				return ok(new GameChatWriter.Response(null));
			}).when(writer).action(anyString(), any());
			doAnswer(invocation -> {
				System.out.println("Tillerino: " + invocation.getArguments()[0]);
				return ok(new GameChatWriter.Response(null));
			}).when(writer).message(anyString(), any());
			return writer;
		}
	}

	static ThreadFactory threadFactory(String name) {
		return r -> { Thread thread = new Thread(r, name); thread.setDaemon(true); return thread; };
	}

	static ExecutorService singleThreadExecutor(String name) {
		return Executors.newSingleThreadExecutor(threadFactory(name));
	}

	/**
	 * This method will start a Tillerinobot instance, which communicates via
	 * stdout, with a bogus backend and an API server.
	 * 
	 * @param args
	 *            give a port number to start the API server on that port.
	 *            Otherwise, a random, free port will be chosen.
	 */
	public static void main(String[] args) throws Exception {
		Injector injector = DaggerLocalConsoleTillerinobot_Injector.builder()
				.clockModule(new LocalConsoleTillerinobot.ClockModule(Clock.system()))
				.build();
		MysqlContainer.MysqlDatabaseLifecycle.createSchema();

		URI baseUri = UriBuilder.fromUri("http://localhost/")
				.port(Integer.parseInt(Stream.of(args).findAny().orElse("0"))).build();
		HttpServer apiServer = JdkHttpServerFactory.createHttpServer(baseUri, 
				ResourceConfig.forApplication(injector.botApiDefinition()));
		log.info("Started API at port {}", apiServer.getAddress().getPort());

		singleThreadExecutor("event queue").submit(injector.localGameChatEventQueue());
		singleThreadExecutor("response queue").submit(injector.localGameChatResponseQueue());
		injector.consoleRunner().run();

		apiServer.stop(1);
	}

	@Singleton
	@RequiredArgsConstructor(onConstructor = @__(@Inject))
	static class ConsoleRunner implements GameChatClient, Runnable {
		final @Named("messagePreprocessor") GameChatEventConsumer preprocessor;
		final BotBackend backend;
		final IrcNameResolver resolver;

		final AtomicBoolean running = new AtomicBoolean(true);
		String username;

		@Override
		public void run() {
			log.info("Starting Tillerinobot");

			try (Scanner scanner = new Scanner(System.in)) {
				while (running.get()) {
					try {
						if (!userLoop(scanner)) {
							break;
						}
					} catch (Exception e) {
						throw new ContextedRuntimeException(e);
					}
				}
			}
		}

		/**
		 * @return true for a user change; false to shut down
		 */
		private boolean userLoop(Scanner scanner) throws Exception {
			System.out.println("please provide your name:");
			username = scanner.nextLine();

			if (resolver.resolveIRCName(username) == null
					&& backend instanceof TestBackend testBackend) {
				System.out.println("you're new. I'll have to ask you a couple of questions.");

				System.out.println("are you a donator? (anything for yes)");
				final boolean donator = scanner.nextLine().length() > 0;

				System.out.println("what's your rank?");
				final int rank = Integer.parseInt(scanner.nextLine());

				System.out.println("how much pp do you have?");
				final double pp = Double.parseDouble(scanner.nextLine());

				testBackend.hintUser(username, donator, rank, pp);
				resolver.resolveManually(backend.downloadUser(username).getUserId());
			}

			System.out.println("Welcome to the Tillerinobot simulator");
			System.out.println("To quit, send /q");
			System.out.println("To change users, send /r");
			System.out.println("To fake an /np command, type /np <beatmapid>");
			System.out.println("Use /nps to send the np with an https url instead of an http url");
			System.out.println("-----------------");

			{
				dispatch(new Joined(System.currentTimeMillis(), username, System.currentTimeMillis()));
			}

			return inputLoop(scanner);
		}

		/**
		 * @return true for a user change; false to shut down
		 */
		private boolean inputLoop(Scanner scanner) throws Exception {
			while (running.get()) {
				String line = scanner.nextLine();
				
				if(line.startsWith("/np ")) {
					dispatch(new PrivateAction(System.currentTimeMillis(), username, System.currentTimeMillis(), "is listening to [http://osu.ppy.sh/b/" + line.substring(4) + " title]"));
				} else if(line.startsWith("/nps ")) {
					dispatch(new PrivateAction(System.currentTimeMillis(), username, System.currentTimeMillis(), "is listening to [https://osu.ppy.sh/b/" + line.substring(4) + " title]"));
				} else if(line.startsWith("/q")) {
					running.set(false);
				} else if(line.startsWith("/r")) {
					return true;
				} else {
					dispatch(new PrivateMessage(System.currentTimeMillis(), username, System.currentTimeMillis(), line));
				}
			}
			return false;
		}

		ExecutorService exec = singleThreadExecutor("bot event loop");

		void dispatch(GameChatEvent event) {
			exec.submit(() -> {
				try {
					preprocessor.onEvent(event);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
		}


		@Override
		public Result<GameChatClientMetrics, Error> getMetrics() {
			return ok(new GameChatClientMetrics());
		}
	}
}
