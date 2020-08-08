package tillerino.tillerinobot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.MessageHandlerScheduler.MessageHandlerSchedulerModule;
import org.tillerino.ppaddict.chat.impl.ProcessorsModule;
import org.tillerino.ppaddict.chat.local.InMemoryQueuesModule;
import org.tillerino.ppaddict.chat.local.LocalGameChatEventQueue;
import org.tillerino.ppaddict.chat.local.LocalGameChatResponseQueue;
import org.tillerino.ppaddict.live.JettyWebsocketServerResource;
import org.tillerino.ppaddict.live.LiveActivityEndpoint;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;
import org.tillerino.ppaddict.web.BarePpaddictUserDataService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
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
public class LocalConsoleTillerinobot extends AbstractModule {
	@Override
	protected void configure() {
		install(new CreateInMemoryDatabaseModule());
		install(new TillerinobotConfigurationModule());
		install(new InMemoryQueuesModule());
		install(new ProcessorsModule());
		installMore();

		bind(LiveActivity.class).to(LiveActivityEndpoint.class);
		bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
		bind(Boolean.class).annotatedWith(
				Names.named("tillerinobot.test.persistentBackend")).toInstance(
				true);
		bind(Clock.class).toInstance(createClock());
		bind(int.class).annotatedWith(Names.named("coreSize")).toInstance(1);
		install(new MessageHandlerSchedulerModule());
		bind(AuthenticationService.class).toInstance(new FakeAuthenticationService());
	}

	protected void installMore() {
		// should not be done when in ppaddict
		bind(AbstractPpaddictUserDataService.class).to(BarePpaddictUserDataService.class);
	}

	protected Clock createClock() {
		return Clock.system();
	}

	static ThreadFactory threadFactory(String name) {
		return r -> { Thread thread = new Thread(r, name); thread.setDaemon(true); return thread; };
	}

	@Provides
	@Singleton
	public BeatmapsService getBeatmapsService(BotBackend backend) {
		BeatmapsService beatService = mock(BeatmapsService.class);
		when(beatService.byId(anyInt())).thenAnswer(req -> {
			BeatmapResource res = mock(BeatmapResource.class);
			doAnswer(x -> backend.getBeatmap((Integer) req.getArguments()[0])).when(res).get();
			doAnswer(x -> { System.out.println("Beatmap uploaded: " + x.getArguments()[0]); return null; }).when(res).setFile(anyString());
			return res;
		});
		return beatService;
	}

	@Provides
	@Singleton
	public GameChatWriter writer() throws Exception {
		GameChatWriter writer = mock(GameChatWriter.class);
		doAnswer(invocation -> {
			System.out.println("*Tillerino " + invocation.getArguments()[0]);
			return null;
		}).when(writer).action(anyString(), any());
		doAnswer(invocation -> {
			System.out.println("Tillerino: " + invocation.getArguments()[0]);
			return null;
		}).when(writer).message(anyString(), any());
		return writer;
	}

	@Provides
	@Singleton
	public GameChatClient getRunner(@Named("messagePreprocessor") GameChatEventConsumer preprocessor,
			final BotBackend backend, final IrcNameResolver resolver, EntityManagerFactory emf,
			ThreadLocalAutoCommittingEntityManager em) throws Exception {

		final AtomicBoolean running = new AtomicBoolean(true);

		
		GameChatClient runner = mock(GameChatClient.class);
		doAnswer(new Answer<Void>() {
			String username;

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				log.info("Starting Tillerinobot");

				try (Scanner scanner = new Scanner(System.in)) {
					while (running.get()) {
						if (!userLoop(scanner)) {
							break;
						}
					}
					return null;
				}
			}

			/**
			 * @return true for a user change; false to shut down
			 */
			private boolean userLoop(Scanner scanner) throws Exception {
				System.out.println("please provide your name:");
				username = scanner.nextLine();

				em.setThreadLocalEntityManager(emf.createEntityManager());
				if (resolver.resolveIRCName(username) == null
						&& backend instanceof TestBackend) {
					System.out.println("you're new. I'll have to ask you a couple of questions.");

					System.out.println("are you a donator? (anything for yes)");
					final boolean donator = scanner.nextLine().length() > 0;

					System.out.println("what's your rank?");
					final int rank = Integer.parseInt(scanner.nextLine());

					System.out.println("how much pp do you have?");
					final double pp = Double.parseDouble(scanner.nextLine());

					((TestBackend) backend).hintUser(username, donator, rank,
							pp);
					resolver.resolveManually(backend.downloadUser(username).getUserId());
				}
				em.close();

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
						runner.disconnectSoftly();
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
		}).when(runner).run();
		doAnswer(x -> {
			running.set(false);
			return null;
		}).when(runner).disconnectSoftly();
		doAnswer(x -> running.get()).when(runner).isConnected();
		return runner;
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
		Injector injector = Guice.createInjector(new LocalConsoleTillerinobot());

		URI baseUri = UriBuilder.fromUri("http://localhost/")
				.port(Integer.parseInt(Stream.of(args).findAny().orElse("0"))).build();
		Server apiServer = JettyHttpContainerFactory.createServer(baseUri, 
				ResourceConfig.forApplication(injector.getInstance(BotApiDefinition.class)));
		((QueuedThreadPool) apiServer.getThreadPool()).setMaxThreads(32);
		apiServer.start();

		JettyWebsocketServerResource websocketServer = new JettyWebsocketServerResource("localhost", 0);
		websocketServer.start();
		websocketServer.addEndpoint(injector.getInstance(LiveActivityEndpoint.class));

		singleThreadExecutor("event queue").submit(injector.getInstance(LocalGameChatEventQueue.class));
		singleThreadExecutor("response queue").submit(injector.getInstance(LocalGameChatResponseQueue.class));
		injector.getInstance(GameChatClient.class).run();
		
		apiServer.stop();
		websocketServer.stop();
	}
}
