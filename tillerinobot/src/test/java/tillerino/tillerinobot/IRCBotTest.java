package tillerino.tillerinobot;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.Bouncer;

import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.CommandHandler.ResponseList;
import tillerino.tillerinobot.CommandHandler.Success;
import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Model;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.testutil.SynchronousExecutorServiceRule;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;

public class IRCBotTest extends AbstractDatabaseTest {

	protected PrivateAction action(String nick, String action) {
		return new PrivateAction(123, nick, 456, action);
	}

	protected PrivateMessage message(String nick, String message) {
		return new PrivateMessage(123, nick, 456, message);
	}

	protected Joined join(String nick) {
		return new Joined(123, nick, 456);
	}

	@Rule
	public SynchronousExecutorServiceRule exec = new SynchronousExecutorServiceRule();

	UserDataManager userDataManager;

	RateLimiter rateLimiter = new RateLimiter();

	@Spy
	TestBackend backend = new TestBackend(false);

	IrcNameResolver resolver;

	RecommendationsManager recommendationsManager;

	@Mock
	LiveActivityEndpoint liveActivity;	

	/**
	 * Contains the messages and actions sent by the bot. At the end of each
	 * test, it must be empty or the test fails.
	 */
	@Mock
	GameChatResponseQueue queue;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);

		resolver = new IrcNameResolver(userNameMappingRepo, backend);

		recommendationsManager = spy(new RecommendationsManager(backend, recommendationsRepo, em, new RecommendationRequestParser(backend)));

		makeQueuePrint();
	}

	void makeQueuePrint() throws InterruptedException {
		doAnswer(x -> { System.out.printf("sending %s in response to %s%n", x.getArguments()[0], x.getArguments()[1]); return null; })
			.when(queue).onResponse(any(), any());
	}

	@After
	public void tearDown() {
		verifyNoMoreInteractions(queue);
		if (userDataManager != null) {
			userDataManager.tidyUp(false);
		}
	}
	
	IRCBot getTestBot(BotBackend backend) {
		RecommendationsManager recMan;
		if (backend == this.backend) {
			recMan = this.recommendationsManager;
		} else {
			recMan = spy(new RecommendationsManager(backend,
					recommendationsRepo, em, new RecommendationRequestParser(backend)));
		}

		IRCBot ircBot = new IRCBot(backend, recMan, userDataManager = new UserDataManager(backend, emf, em, userDataRepository),
				em, emf, resolver, new TestOsutrackDownloader(),
				exec, rateLimiter, liveActivity, queue, mock(Bouncer.class)) {{
		}};
		return ircBot;
	}
	
	@Test
	public void testVersionMessage() throws Exception {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 0, 0);
		backend.setLastVisitedVersion("user", 0);
		
		verifyResponse(bot, message("user", "!recommend"), new CommandHandler.Message(IRCBot.VERSION_MESSAGE).then(singleResponse()));
		verify(backend, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.CURRENT_VERSION));

		verifyResponse(bot, message("user", "!recommend"), singleResponse());
	}
	
	@Test
	public void testWrongStrings() throws Exception {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 100, 1000);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

		verifyResponse(bot, message("user", "!recommend"), successContaining("http://osu.ppy.sh"));
		verifyResponse(bot, message("user", "!r"), successContaining("http://osu.ppy.sh"));
		verifyResponse(bot, message("user", "!recccomend"), messageContaining("!help"));
		verifyResponse(bot, message("user", "!halp"), successContaining("twitter"));
		verifyResponse(bot, message("user", "!feq"), successContaining("FAQ"));
	}

	/**
	 * Just checks that nothing crashes without an actual command.
	 */
	@Test
	public void testNoCommand() throws Exception {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 100, 1000);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

		verifyResponse(bot, message("user", "no command"), Response.none());
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		
		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUserName()).thenReturn("TheDonator");

		this.backend.hintUser("TheDonator", true, 1, 1);
		int userid = resolver.getIDByUserName("TheDonator");
		when(backend.getUser(eq(userid), anyLong())).thenReturn(osuApiUser);
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		
		IRCBot bot = getTestBot(backend);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 1000);
		verifyResponse(bot, join("TheDonator"), new Message("beep boop"));

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 10 * 60 * 1000);
		verifyResponse(bot, join("TheDonator"), new Message("Welcome back, TheDonator."));

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 2l * 24 * 60 * 60 * 1000);
		verifyResponse(bot, join("TheDonator"), messageContaining("TheDonator, "));

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 8l * 24 * 60 * 60 * 1000);
		verifyResponse(bot, join("TheDonator"), messageContaining("TheDonator")
				.then(messageContaining("so long").then(messageContaining("back"))));
	}

	@Test
	public void testHugs() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

		backend.hintUser("donator", true, 0, 0);

		verifyResponse(bot, message("donator", "I need a hug :("),
				new Message("Come here, you!").then(new Action("hugs donator")));
	}
	
	@Test
	public void testComplaint() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

		backend.hintUser("user", false, 0, 1000);

		verifyResponse(bot, message("user", "!r"), anyResponse());

		verifyResponse(bot, message("user", "!complain"), successContaining("complaint"));
	}

	@Test
	public void testResetHandler() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

		backend.hintUser("user", false, 0, 1000);
		
		verifyResponse(bot, message("user", "!reset"), anyResponse());

		Integer id = resolver.resolveIRCName("user");

		verify(recommendationsManager).forgetRecommendations(id);
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

			@Override
			public int getLastVisitedVersion(String nick) throws SQLException, UserException {
				return IRCBot.CURRENT_VERSION;
			}
		};
		IRCBot bot = getTestBot(backend);

		backend.hintUser("user", false, 0, 1000);

		verifyResponse(bot, message("user", "!r"), successContaining("/b/1"));

		verifyResponse(bot, message("user", "!r"), messageContaining("!reset"));

		verifyResponse(bot, message("user", "!r"), messageContaining("!reset"));

		verifyResponse(bot, message("user", "!reset"), anyResponse());

		verifyResponse(bot, message("user", "!r"), successContaining("/b/1"));
	}

	@Test
	public void testGammaDefault() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		backend.hintUser("user", false, 75000, 1000);

		verifyResponse(bot, message("user", "!R"), anyResponse());

		verify(backend).loadRecommendations(anyInt(),
				anyCollectionOf(Integer.class),
				eq(Model.GAMMA), anyBoolean(), anyLong());
	}

	@Test
	public void testGammaDefaultSub100k() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		backend.hintUser("user", false, 125000, 1000);

		verifyResponse(bot, message("user", "!R"), anyResponse());

		verify(backend).loadRecommendations(anyInt(),
				anyCollectionOf(Integer.class),
				eq(Model.GAMMA), anyBoolean(), anyLong());
	}

	@Test
	public void testOsutrack1() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		backend.hintUser("oliebol", false, 125000, 1000);

		verifyResponse(bot, message("oliebol", "!u"), new Success(
				"Rank: +0 (+0.00 pp) in 0 plays. | View detailed data on [https://ameobea.me/osutrack/user/oliebol osu!track]."));
	}

	@Test
	public void testOsutrack2() throws Exception {
		IRCBot bot = getTestBot(backend);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		backend.hintUser("fartownik", false, 125000, 1000);

		verifyResponse(bot, message("fartownik", "!u"),
				new Success("Rank: -3 (+26.25 pp) in 1568 plays. | View detailed data on [https://ameobea.me/osutrack/user/fartownik osu!track].")
				.then(new Message("2 new highscores:[https://osu.ppy.sh/b/768986 #7]: 414.06pp; [https://osu.ppy.sh/b/693195 #89]: 331.89pp; View your recent hiscores on [https://ameobea.me/osutrack/user/fartownik osu!track].")));
	}

	@Test
	public void testAutomaticNameChangeRemapping() throws Exception {
		// override test backend because we need more control
		BotBackend backend = mock(BotBackend.class);
		resolver = new IrcNameResolver(userNameMappingRepo, backend);
		IRCBot bot = getTestBot(backend);

		when(backend.downloadUser("user1_old")).thenReturn(user(1, "user1 old"));
		when(backend.getUser(eq(1), anyLong())).thenReturn(user(1, "user1 old"));
		assertEquals(1, (int) bot.getUserOrThrow("user1_old").getUserId());

		// meanwhile, user 1 changed her name
		when(backend.downloadUser("user1_new")).thenReturn(user(1, "user1 new"));
		when(backend.getUser(eq(1), anyLong())).thenReturn(user(1, "user1 new"));
		// and user 2 hijacked her old name
		when(backend.downloadUser("user1_old")).thenReturn(user(2, "user1 old"));
		when(backend.getUser(eq(2), anyLong())).thenReturn(user(2, "user1 new"));

		assertEquals(2, (int) bot.getUserOrThrow("user1_old").getUserId());
		assertEquals(1, (int) bot.getUserOrThrow("user1_new").getUserId());
	}

	OsuApiUser user(@UserId int id, @OsuName String name) {
		OsuApiUser user = new OsuApiUser();
		user.setUserId(id);
		user.setUserName(name);
		return user;
	}

	private static Response anyResponse() {
		return new Response() {
			@Override
			public boolean equals(Object arg0) {
				return true;
			}

			@Override
			public String toString() {
				return "Any response";
			}
		};
	}

	private static Response singleResponse() {
		return new Response() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 != null && !(arg0 instanceof ResponseList);
			}

			@Override
			public String toString() {
				return "Any single response";
			}
		};
	}

	private static Response messageContaining(String s) {
		return new Response() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 instanceof Message && ((Message) arg0).getContent().contains(s);
			}

			@Override
			public String toString() {
				return "Message containing " + s;
			}
		};
	}

	private static Response successContaining(String s) {
		return new Response() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 instanceof Success && ((Success) arg0).getContent().contains(s);
			}

			@Override
			public String toString() {
				return "Success containing " + s;
			}
		};
	}

	private void verifyResponse(IRCBot bot, GameChatEvent event, Response response) throws InterruptedException {
		verifyNoMoreInteractions(queue);
		reset(queue);
		makeQueuePrint();
		bot.onEvent(event);
		verify(queue).onResponse(response, event);
	}
}
