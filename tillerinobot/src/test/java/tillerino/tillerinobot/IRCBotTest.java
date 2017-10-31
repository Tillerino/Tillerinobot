package tillerino.tillerinobot;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.AsyncTask;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;

public class IRCBotTest extends AbstractDatabaseTest {
	UserDataManager userDataManager;

	@Rule
	public SynchronousExecutorService exec = new SynchronousExecutorService();
	
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
				userDataManager = new UserDataManager(backend, emf, em, userDataRepository), mock(Pinger.class), false, em,
				emf, resolver, new TestOsutrackDownloader(), exec, new RateLimiter());
		return ircBot;
	}
	
	@Before
	public void mockBackend() {
		MockitoAnnotations.initMocks(this);
		
		resolver = new IrcNameResolver(userNameMappingRepo, backend);
		
		recommendationsManager = spy(new RecommendationsManager(backend, recommendationsRepo, em));
	}
	
	@After
	public void tidyUserDataManager() {
		if (userDataManager != null) {
			userDataManager.tidyUp(false);
		}
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

	@Test
	public void testOsutrack1() throws SQLException, IOException,
			UserException {
		IRCBot bot = getTestBot(backend);
		backend.hintUser("oliebol", false, 125000, 1000);

		IRCBotUser botUser = mockBotUser("oliebol");

		bot.processPrivateMessage(botUser, "!u");
		verify(botUser, times(1)).message(eq("Rank: +0 (+0.00 pp) in 0 plays. | View detailed data on [https://ameobea.me/osutrack/user/oliebol osu!track]."), anyBoolean());
	}

	@Test
	public void testOsutrack2() throws SQLException, IOException,
			UserException {
		IRCBot bot = getTestBot(backend);
		backend.hintUser("fartownik", false, 125000, 1000);

		IRCBotUser botUser = mockBotUser("fartownik");

		bot.processPrivateMessage(botUser, "!u");
		verify(botUser, times(1)).message(eq("Rank: -3 (+26.25 pp) in 1568 plays. | View detailed data on [https://ameobea.me/osutrack/user/fartownik osu!track]."), anyBoolean());
		verify(botUser, times(1)).message(eq("2 new highscores:[https://osu.ppy.sh/b/768986 #7]: 414.06pp; [https://osu.ppy.sh/b/693195 #89]: 331.89pp; View your recent hiscores on [https://ameobea.me/osutrack/user/fartownik osu!track]."), anyBoolean());
	}

	@Test
	public void testAsyncTask() throws Exception {
		IRCBot bot = new IRCBot(null, null, null, null, null, false, em, emf, null, null, exec, null);

		EntityManager targetEntityManager = em.getTargetEntityManager();

		AtomicBoolean executed = new AtomicBoolean();
		bot.sendResponse((AsyncTask) () -> {
			assertNotSame(targetEntityManager, em.getTargetEntityManager());
			executed.set(true);
		}, null);
		assertTrue(executed.get());
	}
}
