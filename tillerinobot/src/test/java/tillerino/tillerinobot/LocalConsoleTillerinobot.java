package tillerino.tillerinobot;

import static org.mockito.Mockito.*;

import java.util.Scanner;

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

import tillerino.tillerinobot.IRCBot.Pinger;
import tillerino.tillerinobot.rest.BotInfoService;

public class LocalConsoleTillerinobot {
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(System.in);
		
		TestBackend backend = new TestBackend(true);
		
		System.out.println("please provide your name:");
		final String username = scanner.nextLine();
		
		if(backend.resolveIRCName(username) == null) {
			System.out.println("you're new. I'll have to ask you a couple of questions.");
			
			System.out.println("are you a donator? (anything for yes)");
			final boolean donator = scanner.nextLine().length() > 0;
			
			System.out.println("what's your rank?");
			final int rank = Integer.parseInt(scanner.nextLine());
			
			System.out.println("how much pp do you have?");
			final double pp = Double.parseDouble(scanner.nextLine());
			
			backend.hintUser(username, donator, rank, pp);
		}
		
		System.out.println("Welcome to the Tillerinobot simulator");
		System.out.println("To quit, send /q");
		System.out.println("To fake an /np command, type /np <beatmapid>");
		System.out.println("-----------------");
		
		BotAPIServer apiServer = mock(BotAPIServer.class);
		apiServer.botInfo = mock(BotInfoService.class);
		
		final IRCBot bot = new IRCBot(backend, "x", 999, "x", null, null, true, false, apiServer);
		bot.pinger = mock(Pinger.class);
		bot.bot = mock(PircBotX.class);
		OutputIRC outputRaw = mock(OutputIRC.class);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				@SuppressWarnings("unchecked")
				DisconnectEvent<PircBotX> event = mock(DisconnectEvent.class);
				bot.onEvent(event);
				return null;
			}
		}).when(outputRaw).quitServer();
		when(bot.bot.sendIRC()).thenReturn(outputRaw);
		
		User user = mock(User.class);
		
		when(user.getNick()).thenReturn(username);
		OutputUser outputUser = mock(OutputUser.class);
		when(user.send()).thenReturn(outputUser);
		
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				System.out.println("* Tillerino " + invocation.getArguments()[0]);
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
		
		{
			// JOIN
			@SuppressWarnings("unchecked")
			JoinEvent<PircBotX> event = mock(JoinEvent.class);
			when(event.getUser()).thenReturn(user);
			bot.onEvent(event);
		}
		
		for(;;) {
			String line = scanner.nextLine();
			
			if(line.startsWith("/np ")) {
				@SuppressWarnings("unchecked")
				ActionEvent<PircBotX> event = mock(ActionEvent.class);
				when(event.getUser()).thenReturn(user);
				when(event.getMessage()).thenReturn("is listening to [http://osu.ppy.sh/b/" + line.substring(4) + " title]");
				bot.onEvent(event);
			} else if(line.startsWith("/q")) {
				bot.shutDown();
				break;
			} else {
				@SuppressWarnings("unchecked")
				PrivateMessageEvent<PircBotX> event = mock(PrivateMessageEvent.class);
				when(event.getUser()).thenReturn(user);
				when(event.getMessage()).thenReturn(line);
				bot.onEvent(event);
			}
		}
	}
}
