package tillerino.tillerinobot;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pircbotx.Configuration;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.UnknownEvent;
import org.pircbotx.output.OutputIRC;
import org.pircbotx.output.OutputUser;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.rest.AuthenticationService.Authorization;

import tillerino.tillerinobot.AbstractDatabaseTest.CreateInMemoryDatabaseModule;
import tillerino.tillerinobot.BotRunnerImpl.CloseableBot;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.rest.BeatmapResource;
import tillerino.tillerinobot.rest.BeatmapsService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;

/**
 * The purpose of this class and its main function is to completely mock backend
 * and IRC connection to quickly check out the functionality of Tillerinobot.
 * 
 * @author Tillerino
 * 
 */
@Slf4j
public class LocalConsoleTillerinobot extends AbstractModule {
	static class PircBotX extends CloseableBot {
		public PircBotX(
				Configuration<? extends org.pircbotx.PircBotX> configuration) {
			super(configuration);
		}

		@Override
		public void sendRawLineToServer(String line) {
			super.sendRawLineToServer(line);
		}
	}

	@Override
	protected void configure() {
		install(new CreateInMemoryDatabaseModule());
		install(new TillerinobotConfigurationModule());
		
		bind(Boolean.class).annotatedWith(Names.named("tillerinobot.ignore"))
				.toInstance(false);
		bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
		bind(Boolean.class).annotatedWith(
				Names.named("tillerinobot.test.persistentBackend")).toInstance(
				true);
		bind(ExecutorService.class).annotatedWith(Names.named("tillerinobot.maintenance"))
				.toInstance(Executors.newSingleThreadExecutor(r -> { Thread thread = new Thread(r); thread.setDaemon(true); return thread; }));
		bind(AuthenticationService.class).toInstance(key -> {
			if (key.equals("testKey")) {
				return new Authorization(false);
			}
			throw new NotFoundException();
		});
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
	public BotRunner getRunner(final IRCBot bot, final BotBackend backend,
			final IrcNameResolver resolver, EntityManagerFactory emf,
			ThreadLocalAutoCommittingEntityManager em,
			@Named("tillerinobot.git.commit.id.abbrev") String commit,
			@Named("tillerinobot.git.commit.message.short") String commitMessage) throws Exception {
		final PircBotX pircBot = mock(PircBotX.class);
		when(pircBot.isConnected()).thenReturn(true);
		when(pircBot.getSocket()).thenReturn(mock(Socket.class));

		doAnswer(invocation -> {
			String message = (String) invocation.getArguments()[0];

			@SuppressWarnings("unchecked")
			UnknownEvent<PircBotX> event = mock(UnknownEvent.class);

			Thread.sleep((long) (Math.random() * 1000));

			when(event.getLine()).thenReturn(" PONG" + message.substring(4));

			bot.pinger.handleUnknownEvent(event);

			return null;
		}).when(pircBot).sendRawLineToServer(anyString());

		final AtomicBoolean running = new AtomicBoolean(true);

		{
			// QUITTING
    		OutputIRC outputIRC = mock(OutputIRC.class);
			doAnswer(invocation -> {
				@SuppressWarnings("unchecked")
				DisconnectEvent<PircBotX> event = mock(DisconnectEvent.class);
				bot.onEvent(event);
				running.set(false);
				return null;
			}).when(outputIRC).quitServer();
    		when(pircBot.sendIRC()).thenReturn(outputIRC);
		}
		
		final User user = mock(User.class);
		when(user.getBot()).thenReturn(pircBot);
		
		{
			// USER MESSAGES AND ACTIONS
    		OutputUser outputUser = mock(OutputUser.class);
    		when(user.send()).thenReturn(outputUser);
			doAnswer(invocation -> {
				System.out.println("*Tillerino " + invocation.getArguments()[0]);
				return null;
			}).when(outputUser).action(anyString());
    		
			doAnswer(invocation -> {
				System.out.println("Tillerino: " + invocation.getArguments()[0]);
				return null;
			}).when(outputUser).message(anyString());
		}
		
		BotRunner runner = mock(BotRunner.class);
		when(runner.getBot()).thenReturn(pircBot);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				log.info("Starting Tillerinobot {}: {}", commit, commitMessage);
				@SuppressWarnings("unchecked")
				ConnectEvent<PircBotX> event = mock(ConnectEvent.class);
				when(event.getBot()).thenReturn(pircBot);
				dispatch(event);

				try (Scanner scanner = new Scanner(System.in)) {
					for (; running.get() && userLoop(scanner);)
						;
					return null;
				}
			}

			private boolean userLoop(Scanner scanner) throws Exception {
				System.out.println("please provide your name:");
				final String username = scanner.nextLine();
				when(user.getNick()).thenReturn(username);

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
					// JOIN EVENT
					@SuppressWarnings("unchecked")
					JoinEvent<PircBotX> event = mock(JoinEvent.class);
					when(event.getBot()).thenReturn(pircBot);
					when(event.getUser()).thenReturn(user);
					dispatch(event);
				}

				if (inputLoop(scanner)) {
					return false;
				} else {
					return true;
				}
			}
			
			private boolean inputLoop(Scanner scanner) throws Exception {
				for (; running.get();) {
					String line = scanner.nextLine();
					
					if(line.startsWith("/np ")) {
						@SuppressWarnings("unchecked")
						ActionEvent<PircBotX> event = mock(ActionEvent.class);
						when(event.getUser()).thenReturn(user);
						when(event.getBot()).thenReturn(pircBot);
						when(event.getTimestamp()).thenReturn(System.currentTimeMillis());
						when(event.getMessage()).thenReturn("is listening to [http://osu.ppy.sh/b/" + line.substring(4) + " title]");
						dispatch(event);
					} else if(line.startsWith("/nps ")) {
						@SuppressWarnings("unchecked")
						ActionEvent<PircBotX> event = mock(ActionEvent.class);
						when(event.getUser()).thenReturn(user);
						when(event.getBot()).thenReturn(pircBot);
						when(event.getTimestamp()).thenReturn(System.currentTimeMillis());
						when(event.getMessage()).thenReturn("is listening to [https://osu.ppy.sh/b/" + line.substring(5) + " title]");
						dispatch(event);
					} else if(line.startsWith("/q")) {
						pircBot.sendIRC().quitServer();
						} else if (line.startsWith("/r")) {
							return false;
					} else {
						@SuppressWarnings("unchecked")
						PrivateMessageEvent<PircBotX> event = mock(PrivateMessageEvent.class);
						when(event.getUser()).thenReturn(user);
						when(event.getBot()).thenReturn(pircBot);
						when(event.getMessage()).thenReturn(line);
						when(event.getTimestamp()).thenReturn(System.currentTimeMillis());
						dispatch(event);
					}
				}
				return true;
			}
			
			ExecutorService exec = Executors.newCachedThreadPool(r -> {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			});

			void dispatch(@SuppressWarnings("rawtypes") final Event e) {
				exec.submit(() -> {
					try {
						bot.onEvent(e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				});
			}
		}).when(runner).run();
		return runner;
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

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(RecommendationsManager.class), null);

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(UserDataManager.class), null);

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(Pinger.MXBean.class), null);

		URI baseUri = UriBuilder.fromUri("http://localhost/")
				.port(Integer.parseInt(Stream.of(args).findAny().orElse("0"))).build();
		Server apiServer = JettyHttpContainerFactory.createServer(baseUri, 
				ResourceConfig.forApplication(injector.getInstance(BotAPIServer.class)));
		((QueuedThreadPool) apiServer.getThreadPool()).setMaxThreads(32);
		apiServer.start();

		injector.getInstance(BotRunner.class).run();
		
		apiServer.stop();
	}
}
