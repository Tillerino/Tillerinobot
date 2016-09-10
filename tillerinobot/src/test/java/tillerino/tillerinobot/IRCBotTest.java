package tillerino.tillerinobot;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;

public class IRCBotTest extends AbstractDatabaseTest {
	@Test
	public void testVersionMessage() throws IOException, SQLException, UserException {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 0, 0);
		backend.setLastVisitedVersion("user", 0);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		when(user.message(anyString(), anyBoolean())).thenReturn(true);
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user).message(IRCBot.versionMessage, false);
		verify(backend, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.currentVersion));
		
		user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user, never()).message(IRCBot.versionMessage, false);
	}
	
	@Test
	public void testWrongStrings() throws IOException, SQLException, UserException {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 100, 1000);
		doReturn(IRCBot.currentVersion).when(backend).getLastVisitedVersion(anyString());

		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		
		InOrder inOrder = inOrder(user);

		bot.processPrivateMessage(user, "!recommend");
		inOrder.verify(user).message(contains("http://osu.ppy.sh"), anyBoolean());
		
		bot.processPrivateMessage(user, "!r");
		inOrder.verify(user).message(contains("http://osu.ppy.sh"), anyBoolean());
		
		bot.processPrivateMessage(user, "!recccomend");
		inOrder.verify(user).message(contains("!help"), anyBoolean());
		
		bot.processPrivateMessage(user, "!halp");
		inOrder.verify(user).message(contains("twitter"), anyBoolean());
		
		bot.processPrivateMessage(user, "!feq");
		inOrder.verify(user).message(contains("FAQ"), anyBoolean());
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		
		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUserName()).thenReturn("TheDonator");

		this.backend.hintUser("TheDonator", true, 1, 1);
		int userid = resolver.getIDByUserName("TheDonator");
		when(backend.getUser(eq(userid), anyLong())).thenReturn(osuApiUser);
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("TheDonator");
		
		IRCBot bot = getTestBot(backend);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("beep boop"), anyBoolean());

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 10 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message("Welcome back, TheDonator.", false);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 2l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("TheDonator, "), anyBoolean());
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 8l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(contains("so long"), anyBoolean());
	}
	
	IRCBot getTestBot(BotBackend backend) {
		RecommendationsManager recMan;
		if (backend == this.backend) {
			recMan = this.recommendationsManager;
		} else {
			recMan = spy(new RecommendationsManager(backend,
					recommendationsRepo, em));
		}

		IRCBot ircBot = new IRCBot(backend, recMan, new BotInfo(),
				new UserDataManager(backend), mock(Pinger.class), false, em,
				emf, resolver);
		return ircBot;
	}
	
	@Before
	public void mockBackend() {
		MockitoAnnotations.initMocks(this);
		
		resolver = new IrcNameResolver(userNameMappingRepo, backend);
		
		recommendationsManager = spy(new RecommendationsManager(backend, recommendationsRepo, em));
	}
	
	@Spy
	TestBackend backend = new TestBackend(false);
	
	IrcNameResolver resolver;
	
	RecommendationsManager recommendationsManager;
	
	@Test
	public void testHugs() throws Exception {
		IRCBot bot = getTestBot(backend);

		backend.hintUser("donator", true, 0, 0);

		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn("donator");
	
		bot.processPrivateMessage(botUser, "I need a hug :(");
		
		verify(botUser, times(1)).message("Come here, you!", false);
		verify(botUser).action("hugs donator");
	}
	
	@Test
	public void testComplaint() throws Exception {
		IRCBot bot = getTestBot(backend);

		backend.hintUser("user", false, 0, 1000);

		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn("user");

		bot.processPrivateMessage(botUser, "!r");
		bot.processPrivateMessage(botUser, "!complain");
		
		verify(botUser, times(1)).message(contains("complaint"), anyBoolean());
	}

	@Test
	public void testResetHandler() throws Exception {
		IRCBot bot = getTestBot(backend);

		backend.hintUser("user", false, 0, 1000);
		IRCBotUser botUser = mockBotUser("user");

		bot.processPrivateMessage(botUser, "!reset");

		Integer id = resolver.resolveIRCName("user");

		verify(recommendationsManager).forgetRecommendations(id);
		verify(bot.manager).forgetRecommendations(id);
	}

	IRCBotUser mockBotUser(String name) {
		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn(name);
		when(botUser.message(anyString(), anyBoolean())).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				System.out.println(invocation.getArguments()[0]);
				return true;
			}
		});
		return botUser;
	}

	@Test
	public void testProperEmptySamplerHandling() throws Exception {
		TestBackend backend = new TestBackend(false) {
			@Override
			public Collection<BareRecommendation> loadRecommendations(
					int userid, Collection<Integer> exclude, Model model,
					boolean nomod, long requestMods) throws SQLException,
					IOException, UserException {
				if (exclude.contains(1)) {
					return Collections.emptyList();
				}

				BareRecommendation bareRecommendation = mock(BareRecommendation.class);
				when(bareRecommendation.getProbability()).thenReturn(1d);
				when(bareRecommendation.getBeatmapId()).thenReturn(1);
				return Arrays.asList(bareRecommendation);
			}
		};
		IRCBot bot = getTestBot(backend);

		backend.hintUser("user", false, 0, 1000);

		IRCBotUser botUser = mockBotUser("user");

		bot.processPrivateMessage(botUser, "!r");
		verify(botUser, times(1)).message(contains("/b/1"), anyBoolean());

		bot.processPrivateMessage(botUser, "!r");
		verify(botUser, times(1)).message(contains("!reset"), anyBoolean());
		verify(botUser, times(1)).message(contains("/b/1"), anyBoolean());

		bot.processPrivateMessage(botUser, "!r");
		verify(botUser, times(2)).message(contains("!reset"), anyBoolean());
		verify(botUser, times(1)).message(contains("/b/1"), anyBoolean());

		bot.processPrivateMessage(botUser, "!reset");

		bot.processPrivateMessage(botUser, "!r");
		verify(botUser, times(2)).message(contains("/b/1"), anyBoolean());
	}

	@Test
	public void testGammaDefault() throws SQLException, IOException,
			UserException {
		IRCBot bot = getTestBot(backend);
		backend.hintUser("user", false, 75000, 1000);

		bot.processPrivateMessage(mockBotUser("user"), "!R");

		verify(backend).loadRecommendations(anyInt(),
				anyCollectionOf(Integer.class),
				eq(Model.GAMMA), anyBoolean(), anyLong());
	}

	@Test
	public void testGammaDefaultSub100k() throws SQLException, IOException,
			UserException {
		IRCBot bot = getTestBot(backend);
		backend.hintUser("user", false, 125000, 1000);

		bot.processPrivateMessage(mockBotUser("user"), "!R");

		verify(backend).loadRecommendations(anyInt(),
				anyCollectionOf(Integer.class),
				eq(Model.GAMMA), anyBoolean(), anyLong());
	}
}
