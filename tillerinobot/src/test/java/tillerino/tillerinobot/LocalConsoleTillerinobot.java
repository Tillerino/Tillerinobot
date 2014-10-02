package tillerino.tillerinobot;

import static org.mockito.Mockito.*;

import java.util.Scanner;

import javax.inject.Singleton;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.output.OutputIRC;
import org.pircbotx.output.OutputUser;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import tillerino.tillerinobot.IRCBot.Pinger;
import tillerino.tillerinobot.rest.BotInfoService;

public class LocalConsoleTillerinobot extends AbstractModule {
	@Override
	protected void configure() {
		bind(Pinger.class).toInstance(mock(Pinger.class));
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
	public BotRunner getRunner(final IRCBot bot, final BotBackend backend) {
		final PircBotX pircBot = mock(PircBotX.class);
		{
			// QUITTING
    		OutputIRC outputIRC = mock(OutputIRC.class);
    		doAnswer(new Answer<Void>() {
    			@Override
    			public Void answer(InvocationOnMock invocation) throws Throwable {
    				@SuppressWarnings("unchecked")
    				DisconnectEvent<PircBotX> event = mock(DisconnectEvent.class);
    				bot.onEvent(event);
    				return null;
    			}
    		}).when(outputIRC).quitServer();
    		when(pircBot.sendIRC()).thenReturn(outputIRC);
		}
		
		final User user = mock(User.class);
		
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
				Scanner scanner = new Scanner(System.in);
				
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
				System.out.println("To fake an /np command, type /np <beatmapid>");
				System.out.println("-----------------");
				
				{
					// JOIN EVENT
					@SuppressWarnings("unchecked")
					JoinEvent<PircBotX> event = mock(JoinEvent.class);
					when(event.getBot()).thenReturn(pircBot);
					when(event.getUser()).thenReturn(user);
					bot.onEvent(event);
				}
				
				for(;;) {
					String line = scanner.nextLine();
					
					if(line.startsWith("/np ")) {
						@SuppressWarnings("unchecked")
						ActionEvent<PircBotX> event = mock(ActionEvent.class);
						when(event.getUser()).thenReturn(user);
						when(event.getBot()).thenReturn(pircBot);
						when(event.getMessage()).thenReturn("is listening to [http://osu.ppy.sh/b/" + line.substring(4) + " title]");
						bot.onEvent(event);
					} else if(line.startsWith("/q")) {
						pircBot.sendIRC().quitServer();
						break;
					} else {
						@SuppressWarnings("unchecked")
						PrivateMessageEvent<PircBotX> event = mock(PrivateMessageEvent.class);
						when(event.getUser()).thenReturn(user);
						when(event.getBot()).thenReturn(pircBot);
						when(event.getMessage()).thenReturn(line);
						bot.onEvent(event);
					}
				}
				
				return null;
			}
		}).when(runner).run();
		return runner;
	}

	public static void main(String[] args) throws Exception {
		Guice.createInjector(new LocalConsoleTillerinobot())
				.getInstance(BotRunner.class).run();
	}
}
