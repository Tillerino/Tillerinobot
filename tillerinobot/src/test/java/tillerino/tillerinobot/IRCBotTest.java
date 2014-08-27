package tillerino.tillerinobot;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.Pinger;

public class IRCBotTest {
	@Test
	public void testVersionMessage() throws IOException, SQLException, UserException {
		BotBackend mock = mock(BotBackend.class);
		
		when(mock.getLastVisitedVersion(anyString())).thenReturn(1, IRCBot.currentVersion);
		
		IRCBot bot = getTestBot(mock);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("");
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user).message(IRCBot.versionMessage);
		verify(mock, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.currentVersion));
		
		user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("");
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user, never()).message(IRCBot.versionMessage);
	}
	
	@Test
	public void testWrongStrings() throws IOException, SQLException, UserException {
		BotBackend mock = mock(BotBackend.class);
		when(mock.getUser(anyString(), anyLong())).thenReturn(new OsuApiUser());
		
		when(mock.loadRecommendation(anyString(), anyString())).then(new Answer<Recommendation>() {
			@Override
			public Recommendation answer(InvocationOnMock invocation)
					throws Throwable {
				Recommendation recommendation = new Recommendation();
				
				String settings = (String) invocation.getArguments()[1];
				
				OsuApiBeatmap beatmap = new OsuApiBeatmap();
				if(settings.contains("nomod")) {
					beatmap.setTitle("nomod");
				} else if(settings.contains("relax")) {
					beatmap.setTitle("relax");
				} else {
					beatmap.setTitle("title");
				}
				
				beatmap.setArtist("artist");
				beatmap.setVersion("version");
				
				BeatmapMeta meta = mock(BeatmapMeta.class);
				when(meta.getBeatmap()).thenReturn(beatmap);
				
				recommendation.beatmap = meta;
				
				return recommendation;
			}
		});
		
		IRCBot bot = getTestBot(mock);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("username");
		
		when(user.message(anyString())).then(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				System.out.println(invocation.getArguments()[0]);
				return true;
			}
		});
		
		InOrder inOrder = inOrder(user);

		bot.processPrivateMessage(user, "!recommend");
		inOrder.verify(user).message(contains("artist - title [version]"));
		
		bot.processPrivateMessage(user, "!r");
		inOrder.verify(user).message(contains("artist - title [version]"));
		
		bot.processPrivateMessage(user, "!r relax");
		inOrder.verify(user).message(contains("artist - relax [version]"));
		
		bot.processPrivateMessage(user, "!r relax nomod");
		inOrder.verify(user).message(contains("artist - nomod [version]"));
		
		bot.processPrivateMessage(user, "!recomend");
		inOrder.verify(user).message(contains("artist - title [version]"));
		
		bot.processPrivateMessage(user, "!reccomend");
		inOrder.verify(user).message(contains("artist - title [version]"));
		
		bot.processPrivateMessage(user, "!recccomend");
		inOrder.verify(user).message(contains("!help"));
		
		bot.processPrivateMessage(user, "!halp");
		inOrder.verify(user).message(contains("twitter"));
		
		bot.processPrivateMessage(user, "!feq");
		inOrder.verify(user).message(contains("FAQ"));
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		
		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUsername()).thenReturn("TheDonator");
		
		when(backend.getUser(anyString(), anyLong())).thenReturn(osuApiUser);
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("TheDonator");
		
		IRCBot bot = getTestBot(backend);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("beep boop"));

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 10 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message("Welcome back, TheDonator.");
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 2l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("TheDonator, "));
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 8l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(contains("so long"));
	}
	
	IRCBot getTestBot(BotBackend backend) {
		IRCBot ircBot = new IRCBot(backend, "server", 1, "botuser", null, null, false, false);
		ircBot.pinger = mock(Pinger.class);
		return ircBot;
	}
	
	@Test
	public void testDonateLink() throws Exception {
		IRCBot bot = getTestBot(mock(BotBackend.class));

		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn("someuser");
		
		bot.processPrivateMessage(botUser, "!donat");

		verify(botUser).message(contains("wiki/Donate"));
	}
	
	@Test
	public void testHugs() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		OsuApiUser user = new OsuApiUser();
		user.setUsername("actualUsername");
		when(backend.getUser(anyString(), anyLong())).thenReturn(user);
		
		IRCBot bot = getTestBot(backend);

		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn("someuser");
	
		bot.processPrivateMessage(botUser, "I need a hug :(");
		
		verify(botUser).message("Come here, you!");
		verify(botUser).action("hugs actualUsername");
		
		verify(botUser, times(2 /* version message also sent */)).message(anyString());
	}
}
