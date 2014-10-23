package tillerino.tillerinobot;

import static org.mockito.Mockito.*;

import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Singleton;
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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import tillerino.tillerinobot.rest.BotInfoService;

/**
 * The purpose of this class and its main function is to completely mock backend
 * and IRC connection to quickly check out the functionality of Tillerinobot.
 * 
 * @author Tillerino
 * 
 */
public class LocalConsoleTillerinobot extends AbstractModule {
	static class PircBotX extends org.pircbotx.PircBotX {
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
		bind(Boolean.class).annotatedWith(Names.named("tillerinobot.ignore"))
				.toInstance(false);
		bind(BotInfoService.class).toInstance(mock(BotInfoService.class));

		bind(BotBackend.class).to(TestBackend.class).in(Singleton.class);
		bind(Boolean.class).annotatedWith(
				Names.named("tillerinobot.test.persistentBackend")).toInstance(
				true);
	}

	@Provides
	@Singleton
	public BotRunner getRunner(final IRCBot bot, final BotBackend backend) throws Exception {
		final PircBotX pircBot = mock(PircBotX.class);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				String message = (String) invocation.getArguments()[0];

				@SuppressWarnings("unchecked")
				UnknownEvent<PircBotX> event = mock(UnknownEvent.class);

				Thread.sleep((long) (Math.random() * 1000));

				when(event.getLine()).thenReturn(" PONG" + message.substring(4));

				bot.pinger.handleUnknownEvent(event);

				return null;
			}
		}).when(pircBot).sendRawLineToServer(anyString());

		final AtomicBoolean running = new AtomicBoolean(true);

		{
			// QUITTING
    		OutputIRC outputIRC = mock(OutputIRC.class);
    		doAnswer(new Answer<Void>() {
    			@Override
    			public Void answer(InvocationOnMock invocation) throws Throwable {
    				@SuppressWarnings("unchecked")
    				DisconnectEvent<PircBotX> event = mock(DisconnectEvent.class);
    				bot.onEvent(event);
					running.set(false);
    				return null;
    			}
    		}).when(outputIRC).quitServer();
    		when(pircBot.sendIRC()).thenReturn(outputIRC);
		}
		
		final User user = mock(User.class);
		when(user.getBot()).thenReturn(pircBot);
		
		{
			// USER MESSAGES AND ACTIONS
    		OutputUser outputUser = mock(OutputUser.class);
    		when(user.send()).thenReturn(outputUser);
    		doAnswer(new Answer<Void>() {
    			@Override
    			public Void answer(InvocationOnMock invocation) throws Throwable {
    				System.out.println("*Tillerino " + invocation.getArguments()[0]);
    				return null;
    			}
    		}).when(outputUser).action(anyString());
    		
    		doAnswer(new Answer<Void>() {
    			@Override
    			public Void answer(InvocationOnMock invocation) throws Throwable {
    				System.out.println("Tillerino: " + invocation.getArguments()[0]);
    				return null;
    			}
    		}).when(outputUser).message(anyString());
		}
		
		BotRunner runner = mock(BotRunner.class);
		when(runner.getBot()).thenAnswer(new Answer<PircBotX>() {
			@Override
			public PircBotX answer(InvocationOnMock invocation)
					throws Throwable {
				return pircBot;
			}
		});
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
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

				if (backend.resolveIRCName(username) == null
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
				}

				System.out.println("Welcome to the Tillerinobot simulator");
				System.out.println("To quit, send /q");
				System.out.println("To change users, send /r");
				System.out
						.println("To fake an /np command, type /np <beatmapid>");
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
						when(event.getMessage()).thenReturn("is listening to [http://osu.ppy.sh/b/" + line.substring(4) + " title]");
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
						dispatch(event);
					}
				}
				return true;
			}
			
			ExecutorService exec = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					return t;
				}
			});

			void dispatch(@SuppressWarnings("rawtypes") final Event e) {
				exec.submit(new Runnable() {
					public void run() {
						try {
							bot.onEvent(e);
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				});
			}
		}).when(runner).run();
		return runner;
	}

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new LocalConsoleTillerinobot());

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(RecommendationsManager.class), null);

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(UserDataManager.class), null);

		ManagementFactory.getPlatformMBeanServer().registerMBean(
				injector.getInstance(Pinger.MXBean.class), null);

		injector.getInstance(BotRunner.class).run();
	}
}
