package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Action;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.Sighted;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.TestModule;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;

import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Model;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.testutil.SynchronousExecutorServiceRule;

@TestModule(value = TestBackend.Module.class, mocks = { LiveActivity.class, GameChatResponseQueue.class })
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

	RateLimiter rateLimiter = new RateLimiter();

	@Inject
	TestBackend backend;

	@Inject
	IrcNameResolver resolver;

	@Inject
	RecommendationsManager recommendationsManager;

	@Inject
	LiveActivity liveActivity;

	/**
	 * Contains the messages and actions sent by the bot. At the end of each
	 * test, it must be empty or the test fails.
	 */
	@Inject
	GameChatResponseQueue queue;

	AbstractPpaddictUserDataService<?> ppaddictUserDataService = mock(AbstractPpaddictUserDataService.class);

	boolean printResponses = false;

	@Before
	public void initMocks() throws Exception {
		mockQueuePrint();
	}

	void mockQueuePrint() throws InterruptedException {
		doAnswer(x -> {
			if (printResponses) {
				System.out.printf("sending %s in response to %s%n", x.getArguments()[0], x.getArguments()[1]);
			}
			return null;
		}).when(queue).onResponse(any(), any());
	}

	@After
	public void tearDown() {
		verifyNoMoreInteractions(queue);
	}
	
	IRCBot getTestBot(BotBackend backend) {
		RecommendationsManager recMan;
		if (backend == this.backend) {
			if (!Mockito.mockingDetails(this.recommendationsManager).isSpy()) {
				this.recommendationsManager = spy(recommendationsManager);
			}
			recMan = this.recommendationsManager;
		} else {
			recMan = spy(new RecommendationsManager(backend, recommendationsRepo, em,
					new RecommendationRequestParser(backend), new TestBackend.TestBeatmapsLoader()));
		}

		IRCBot ircBot = new IRCBot(backend, recMan, new UserDataManager(backend, em, userDataRepository),
				em, resolver, new TestOsutrackDownloader(), rateLimiter,
				liveActivity, queue, ppaddictUserDataService) {{
		}};
		return ircBot;
	}
	
	@Test
	public void testVersionMessage() throws Exception {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 0, 0);
		backend.setLastVisitedVersion("user", 0);
		
		verifyResponse(bot, message("user", "!recommend"), new Message(IRCBot.VERSION_MESSAGE).then(singleResponse()));
		verify(backend, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.CURRENT_VERSION));

		verifyResponse(bot, message("user", "!recommend"), singleResponse());
	}
	
	@Test
	public void testWrongStrings() throws Exception {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 100, 1000);
		turnOffVersionMessage();

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
		turnOffVersionMessage();

		verifyResponse(bot, message("user", "no command"), GameChatResponse.none());
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
		
		this.backend.hintUser("TheDonator", true, 1, 1);
		int userid = resolver.getIDByUserName("TheDonator");

		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUserName()).thenReturn("TheDonator");
		when(osuApiUser.getUserId()).thenReturn(userid);

		when(backend.getUser(eq(userid), anyLong())).thenReturn(osuApiUser);
		when(backend.getDonator(userid)).thenReturn(1);

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
		turnOffVersionMessage();

		backend.hintUser("donator", true, 0, 0);

		verifyResponse(bot, message("donator", "I need a hug :("),
				new Message("Come here, you!").then(new Action("hugs donator")));
	}
	
	@Test
	public void testComplaint() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();

		backend.hintUser("user", false, 0, 1000);

		verifyResponse(bot, message("user", "!r"), anyResponse());

		verifyResponse(bot, message("user", "!complain"), successContaining("complaint"));
	}

	@Test
	public void testResetHandler() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();

		backend.hintUser("user", false, 0, 1000);
		
		verifyResponse(bot, message("user", "!reset"), anyResponse());

		Integer id = resolver.resolveIRCName("user");

		verify(recommendationsManager).forgetRecommendations(id);
	}

	@Test
	public void testProperEmptySamplerHandling() throws Exception {
		TestBackend backend = new TestBackend(false, new TestBackend.TestBeatmapsLoader()) {
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
		turnOffVersionMessage();
		backend.hintUser("user", false, 75000, 1000);

		verifyResponse(bot, message("user", "!R"), anyResponse());

		verify(backend).loadRecommendations(anyInt(),
				any(),
				eq(Model.GAMMA5), anyBoolean(), anyLong());
	}

	@Test
	public void testGammaDefaultSub100k() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();
		backend.hintUser("user", false, 125000, 1000);

		verifyResponse(bot, message("user", "!R"), anyResponse());

		verify(backend).loadRecommendations(anyInt(),
				any(),
				eq(Model.GAMMA5), anyBoolean(), anyLong());
	}

    private static final GameChatResponse OSUTRACK_RESPONSE_OLIEBOL = new Success("Rank: +0 (+0.00 pp) in 0 plays. | View detailed data on [https://ameobea.me/osutrack/user/oliebol osu!track].");
    private static final GameChatResponse OSUTRACK_RESPONSE_FARTOWNIK = new Success("Rank: -3 (+26.25 pp) in 1568 plays. | View detailed data on [https://ameobea.me/osutrack/user/fartownik osu!track].")
            .then(new Message("2 new highscores:[https://osu.ppy.sh/b/768986 #7]: 414.06pp; [https://osu.ppy.sh/b/693195 #89]: 331.89pp; View your recent hiscores on [https://ameobea.me/osutrack/user/fartownik osu!track]."));

    private void hintOsutrackUsers() {
        backend.hintUser("oliebol", false, 125000, 1000, 2756335);
        backend.hintUser("fartownik", false, 125000, 1000, 56917);
    }

    @Test
    public void testOsutrackOliebol() throws Exception {
        IRCBot bot = getTestBot(backend);
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(bot, message("oliebol", "!u"), OSUTRACK_RESPONSE_OLIEBOL);
    }

	@Test
	public void testOsutrackOliebolQueryFartownik() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();
		hintOsutrackUsers();

		verifyResponse(bot, message("oliebol", "!u fartownik"), OSUTRACK_RESPONSE_FARTOWNIK);
	}

	@Test
	public void testOsutrackOliebolQueryNonExistendUser() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();
		hintOsutrackUsers();

		verifyResponse(bot, message("oliebol", "!u doesnotexist"), new Success("User doesnotexist does not exist"));
	}

	@Test
	public void testOsutrackFartownik() throws Exception {
		IRCBot bot = getTestBot(backend);
		turnOffVersionMessage();
        hintOsutrackUsers();

		verifyResponse(bot, message("fartownik", "!u"), OSUTRACK_RESPONSE_FARTOWNIK);
	}

	void turnOffVersionMessage() throws SQLException, UserException {
		doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
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
		when(backend.getUser(eq(1), anyLong())).thenReturn(user(1, "user1 new"));
		// and user 2 hijacked her old name
		when(backend.downloadUser("user1_old")).thenReturn(user(2, "user1 old"));

		assertEquals(2, (int) bot.getUserOrThrow("user1_old").getUserId());
		assertEquals(1, (int) bot.getUserOrThrow("user1_new").getUserId());
	}

	@Test
	public void testMaintenanceOnSight() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		resolver = mock(IrcNameResolver.class);
		when(resolver.resolveIRCName("aRareUserAppears")).thenReturn(18);
		IRCBot bot = getTestBot(backend);

		Sighted event = new Sighted(12, "aRareUserAppears", 15);
		bot.onEvent(event);
		verify(queue).onResponse(GameChatResponse.none(), event);
		verify(backend, timeout(1000)).registerActivity(18, 15);
	}

	@Test
	public void testNp() throws Exception {
		IRCBot bot = getTestBot(backend);
		backend.hintUser("user", false, 1000, 1000);
		turnOffVersionMessage();
		verifyResponse(bot, action("user", "is listening to [https://osu.ppy.sh/b/125 map]"), successContaining("pp"));
	}

	@Test
	public void maintenance() throws Exception {
		IRCBot bot = getTestBot(backend);
		doThrow(MaintenanceException.class).when(backend).loadRecommendations(anyInt(), any(), any(), anyBoolean(), anyLong());
		backend.hintUser("user", false, 1000, 1000);
		turnOffVersionMessage();
		verifyResponse(bot, message("user", "!r"), messageContaining("maintenance"));
	}

	OsuApiUser user(@UserId int id, @OsuName String name) {
		OsuApiUser user = new OsuApiUser();
		user.setUserId(id);
		user.setUserName(name);
		return user;
	}

	private static GameChatResponse anyResponse() {
		return new GameChatResponse() {
			@Override
			public boolean equals(Object arg0) {
				return true;
			}

			@Override
			public String toString() {
				return "Any response";
			}

			@Override
			public Iterable<GameChatResponse> flatten() {
				throw new NotImplementedException("nono");
			}
		};
	}

	private static GameChatResponse singleResponse() {
		return new GameChatResponse() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 != null && !(arg0 instanceof ResponseList);
			}

			@Override
			public String toString() {
				return "Any single response";
			}

			@Override
			public Iterable<GameChatResponse> flatten() {
				throw new NotImplementedException("nono");
			}
		};
	}

	private static GameChatResponse messageContaining(String s) {
		return new GameChatResponse() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 instanceof Message msg && msg.getContent().contains(s);
			}

			@Override
			public String toString() {
				return "Message containing " + s;
			}

			@Override
			public Iterable<GameChatResponse> flatten() {
				throw new NotImplementedException("nono");
			}
		};
	}

	private static GameChatResponse successContaining(String s) {
		return new GameChatResponse() {
			@Override
			public boolean equals(Object arg0) {
				return arg0 instanceof Success suc && suc.getContent().contains(s);
			}

			@Override
			public String toString() {
				return "Success containing " + s;
			}

			@Override
			public Iterable<GameChatResponse> flatten() {
				throw new NotImplementedException("nono");
			}
		};
	}

	private void verifyResponse(IRCBot bot, GameChatEvent event, GameChatResponse response) throws InterruptedException {
		verifyNoMoreInteractions(queue);
		reset(queue);
		mockQueuePrint();
		bot.onEvent(event);
		verify(queue).onResponse(response, event);
	}
}
