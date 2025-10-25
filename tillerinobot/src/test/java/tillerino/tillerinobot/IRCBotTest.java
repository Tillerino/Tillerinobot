package tillerino.tillerinobot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dagger.Component;
import jakarta.ws.rs.InternalServerErrorException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import org.tillerino.ppaddict.config.CachedDatabaseConfigServiceModule;
import org.tillerino.ppaddict.mockmodules.GameChatResponseQueueMockModule;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.osutrack.TestOsutrackDownloader;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Model;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.testutil.SynchronousExecutorServiceRule;

public class IRCBotTest extends AbstractDatabaseTest {
    @Singleton
    @Component(
            modules = {
                DockeredMysqlModule.class,
                TestBackend.Module.class,
                LiveActivityMockModule.class,
                GameChatResponseQueueMockModule.class,
                OsuApiV2Sometimes.Module.class,
                OsuApiV1Test.Module.class,
                OsuApiV2Test.Module.class,
                CachedDatabaseConfigServiceModule.class,
                Clock.Module.class
            })
    interface Injector {

        void inject(IRCBotTest t);
    }

    {
        DaggerIRCBotTest_Injector.create().inject(this);
    }

    protected PrivateAction action(String nick, String action) {
        return preprocess(new PrivateAction(123, nick, 456, action));
    }

    protected PrivateMessage message(String nick, String message) {
        return preprocess(new PrivateMessage(123, nick, 456, message));
    }

    static <T extends GameChatEvent> T preprocess(T event) {
        event.getMeta().setMdc(MdcUtils.getSnapshot());
        event.getMeta().setTimer(new PhaseTimer());
        return event;
    }

    protected Joined join(String nick) {
        return new Joined(123, nick, 456);
    }

    @RegisterExtension
    public SynchronousExecutorServiceRule exec = new SynchronousExecutorServiceRule();

    RateLimiter rateLimiter = new RateLimiter();

    TestOsutrackDownloader osuTrackDownloader = spy(new TestOsutrackDownloader());

    @Inject
    TestBackend backend;

    @Inject
    IrcNameResolver resolver;

    @Inject
    RecommendationsManager recommendationsManager;

    @Inject
    LiveActivity liveActivity;

    @Inject
    Recommender rec;

    @Inject
    UserDataManager userDataManager;

    @Inject
    OsuApiV2 osuApiV2;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    /**
     * Contains the messages and actions sent by the bot. At the end of each test, it must be empty or the test fails.
     */
    @Inject
    GameChatResponseQueue queue;

    @Inject
    PullThrough pullThrough;

    @Inject
    OsuApi downloader;

    @Inject
    PlayerService playerService;

    boolean printResponses = false;

    private IRCBot bot;

    @BeforeEach
    public void initMocks() throws Exception {
        mockQueuePrint();

        this.recommendationsManager = spy(recommendationsManager);
        this.resolver = spy(this.resolver);
        this.bot = new IRCBot(
                this.backend,
                recommendationsManager,
                userDataManager,
                resolver,
                osuTrackDownloader,
                rateLimiter,
                liveActivity,
                queue,
                diffEstimateProvider,
                pullThrough,
                playerService);
    }

    void mockQueuePrint() throws InterruptedException {
        doAnswer(x -> {
                    if (printResponses) {
                        System.out.printf("sending %s in response to %s%n", x.getArguments()[0], x.getArguments()[1]);
                    }
                    return null;
                })
                .when(queue)
                .onResponse(any(), any());
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(queue);
    }

    @Test
    public void testVersionMessage() throws Exception {
        backend.hintUser("user", false, 0, 0);
        backend.setLastVisitedVersion("user", 0);

        verifyResponse(bot, message("user", "!recommend"), new Message(IRCBot.VERSION_MESSAGE).then(singleResponse()));
        verify(backend, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.CURRENT_VERSION));

        verifyResponse(bot, message("user", "!recommend"), singleResponse());
    }

    @Test
    public void testWrongStrings() throws Exception {
        backend.hintUser("user", false, 100, 1000);
        turnOffVersionMessage();

        verifyResponse(bot, message("user", "!recommend"), successContaining("http://osu.ppy.sh"));
        verifyResponse(bot, message("user", "!r"), successContaining("http://osu.ppy.sh"));
        verifyResponse(bot, message("user", "!recccomend"), messageContaining("!help"));
        verifyResponse(bot, message("user", "!halp"), successContaining("twitter"));
        verifyResponse(bot, message("user", "!feq"), successContaining("FAQ"));
    }

    /** Just checks that nothing crashes without an actual command. */
    @Test
    public void testNoCommand() throws Exception {
        backend.hintUser("user", false, 100, 1000);
        turnOffVersionMessage();

        verifyResponse(bot, message("user", "no command"), GameChatResponse.none());
    }

    @Test
    public void testWelcomeIfDonator() throws Exception {
        doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());

        this.backend.hintUser("TheDonator", true, 1, 1);
        int userid = resolver.getIDByUserName("TheDonator");

        ApiUser osuApiUser = mock(ApiUser.class);
        doReturn("TheDonator").when(osuApiUser).getUserName();
        doReturn(userid).when(osuApiUser).getUserId();

        doReturn(osuApiUser).when(pullThrough).getUser(eq(userid), anyLong());
        doReturn(1).when(backend).getDonator(userid);

        doReturn(System.currentTimeMillis() - 1000).when(playerService).getLastActivity(any(OsuApiUser.class));
        verifyResponse(bot, preprocess(join("TheDonator")), new Message("beep boop"));

        doReturn(System.currentTimeMillis() - 10 * 60 * 1000)
                .when(playerService)
                .getLastActivity(any(OsuApiUser.class));
        verifyResponse(bot, preprocess(join("TheDonator")), new Message("Welcome back, TheDonator."));

        doReturn(System.currentTimeMillis() - 2l * 24 * 60 * 60 * 1000)
                .when(playerService)
                .getLastActivity(any(OsuApiUser.class));
        verifyResponse(bot, preprocess(join("TheDonator")), messageContaining("TheDonator, "));

        doReturn(System.currentTimeMillis() - 8l * 24 * 60 * 60 * 1000)
                .when(playerService)
                .getLastActivity(any(OsuApiUser.class));
        verifyResponse(
                bot,
                preprocess(join("TheDonator")),
                messageContaining("TheDonator")
                        .then(messageContaining("so long").then(messageContaining("back"))));
    }

    @Test
    public void testHugs() throws Exception {
        turnOffVersionMessage();

        backend.hintUser("donator", true, 0, 0);

        verifyResponse(
                bot,
                message("donator", "I need a hug :("),
                new Message("Come here, you!").then(new Action("hugs donator")));
    }

    @Test
    public void testComplaint() throws Exception {
        turnOffVersionMessage();

        backend.hintUser("user", false, 0, 1000);

        verifyResponse(bot, message("user", "!r"), anyResponse());

        verifyResponse(bot, message("user", "!complain"), successContaining("complaint"));
    }

    @Test
    public void testResetHandler() throws Exception {
        turnOffVersionMessage();

        backend.hintUser("user", false, 0, 1000);

        verifyResponse(bot, message("user", "!reset"), anyResponse());

        Integer id = resolver.resolveIRCName("user");

        verify(recommendationsManager).forgetRecommendations(id);
    }

    @Test
    public void testProperEmptySamplerHandling() throws Exception {
        doReturn(List.of(new BareRecommendation(1, 0, null, null, 0)))
                .when(rec)
                .loadRecommendations(any(), Mockito.argThat(Collection::isEmpty), any(), anyBoolean(), anyLong());
        doReturn(Collections.emptyList())
                .when(rec)
                .loadRecommendations(any(), Mockito.argThat(l -> !l.isEmpty()), any(), anyBoolean(), anyLong());
        doReturn(Collections.emptyList()).when(rec).loadTopPlays(1);

        doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion("user");

        backend.hintUser("user", false, 0, 1000);

        verifyResponse(bot, message("user", "!r"), successContaining("/b/1"));

        verifyResponse(bot, message("user", "!r"), messageContaining("!reset"));

        verifyResponse(bot, message("user", "!r"), messageContaining("!reset"));

        verifyResponse(bot, message("user", "!reset"), anyResponse());

        verifyResponse(bot, message("user", "!r"), successContaining("/b/1"));
    }

    @Test
    public void testGammaDefault() throws Exception {
        turnOffVersionMessage();
        backend.hintUser("user", false, 75000, 1000);

        verifyResponse(bot, message("user", "!R"), anyResponse());

        verify(rec).loadRecommendations(Mockito.anyList(), any(), eq(Model.GAMMA10), anyBoolean(), anyLong());
    }

    private static final GameChatResponse OSUTRACK_RESPONSE_WITH_SPACE = new Success(
            "Rank: +0 (+0.00 pp) in 0 plays. | View detailed data on [https://ameobea.me/osutrack/user/has+space osu!track].");
    private static final GameChatResponse OSUTRACK_RESPONSE_FARTOWNIK = new Success(
                    "Rank: -3 (+26.25 pp) in 1568 plays. | View detailed data on [https://ameobea.me/osutrack/user/fartownik osu!track].")
            .then(
                    new Message(
                            "2 new highscores:[https://osu.ppy.sh/b/768986 #7]: 414.06pp; [https://osu.ppy.sh/b/693195 #89]: 331.89pp; View your recent hiscores on [https://ameobea.me/osutrack/user/fartownik osu!track]."));

    private void hintOsutrackUsers() {
        backend.hintUser("oliebol", false, 125000, 1000, 2756335);
        backend.hintUser("fartownik", false, 125000, 1000, 56917);
        backend.hintUser("unknown", false, 125000, 1000, 1234);
        backend.hintUser("has space", false, 125000, 1000, 2345);
    }

    @Test
    public void testOsutrackWithSpace() throws Exception {
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(bot, message("has space", "!u"), OSUTRACK_RESPONSE_WITH_SPACE);
    }

    @Test
    public void testOsutrackOliebolQueryFartownik() throws Exception {
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(bot, message("oliebol", "!u fartownik"), OSUTRACK_RESPONSE_FARTOWNIK);
    }

    @Test
    public void testOsutrackOliebolQueryNonExistendUser() throws Exception {
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(bot, message("oliebol", "!u doesnotexist"), new Success("User doesnotexist does not exist"));
    }

    @Test
    public void testOsutrackFartownik() throws Exception {
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(bot, message("fartownik", "!u"), OSUTRACK_RESPONSE_FARTOWNIK);
    }

    @Test
    public void testOsutrackUnknown() throws Exception {
        turnOffVersionMessage();
        hintOsutrackUsers();

        verifyResponse(
                bot,
                message("unknown", "!u"),
                new Message(
                        "osu!track doesn't know you. Try searching for your user here first: https://ameobea.me/osutrack/"));
    }

    @Test
    public void testOsutrackServerError() throws Exception {
        turnOffVersionMessage();
        backend.hintUser("unknown", false, 125000, 1000, 1234);
        doThrow(new InternalServerErrorException()).when(osuTrackDownloader).getUpdate(1234);

        verifyResponse(
                bot,
                message("unknown", "!u"),
                new Message(
                        "osu!track doesn't seem to be working right now. Maybe try your luck on the website: https://ameobea.me/osutrack/"));
    }

    void turnOffVersionMessage() throws SQLException, UserException {
        doReturn(IRCBot.CURRENT_VERSION).when(backend).getLastVisitedVersion(anyString());
    }

    @Test
    public void testAutomaticNameChangeRemapping() throws Exception {
        doReturn(user(1, "user1 old")).when(pullThrough).downloadUser("user1_old");
        doReturn(user(1, "user1 old")).when(pullThrough).getUser(eq(1), anyLong());
        assertEquals(1, (int) bot.getUserOrThrow("user1_old").getUserId());

        // meanwhile, user 1 changed her name
        doReturn(user(1, "user1 new")).when(pullThrough).getUser(eq(1), anyLong());
        // and user 2 hijacked her old name
        doReturn(user(2, "user1 old")).when(pullThrough).downloadUser("user1_old");

        assertEquals(2, (int) bot.getUserOrThrow("user1_old").getUserId());
        assertEquals(1, (int) bot.getUserOrThrow("user1_new").getUserId());
    }

    @Test
    public void testMaintenanceOnSight() throws Exception {
        doReturn(18).when(resolver).resolveIRCName("aRareUserAppears");
        doAnswer(x -> null).when(playerService).registerActivity(eq(18), anyLong());

        Sighted event = preprocess(new Sighted(12, "aRareUserAppears", 15));
        bot.onEvent(event);
        verify(queue).onResponse(GameChatResponse.none(), event);
        verify(playerService, timeout(1000)).registerActivity(18, 15);
    }

    @Test
    public void testNp() throws Exception {
        backend.hintUser("user", false, 1000, 1000);
        turnOffVersionMessage();
        verifyResponse(bot, action("user", "is listening to [https://osu.ppy.sh/b/125 map]"), successContaining("pp"));
    }

    @Test
    public void maintenance() throws Exception {
        doThrow(MaintenanceException.class).when(rec).loadTopPlays(1);
        backend.hintUser("user", false, 1000, 1000);
        turnOffVersionMessage();
        verifyResponse(bot, message("user", "!r"), messageContaining("maintenance"));
    }

    @Test
    public void v2ApiTriggersUpdate() throws Exception {
        turnOffVersionMessage();

        backend.hintUser("user", false, 1000, 1000, 2070907);
        verifyResponse(bot, message("user", "!set v2 on"), messageContaining("v2 API: ON"));
        verify(osuApiV2).getUserTop(2070907, 0, 50);
    }

    ApiUser user(@UserId int id, @OsuName String name) {
        ApiUser user = new ApiUser();
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
                return arg0 instanceof Message msg && msg.content().contains(s);
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
                return arg0 instanceof Success suc && suc.content().contains(s);
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

    private void verifyResponse(IRCBot bot, GameChatEvent event, GameChatResponse response)
            throws InterruptedException {
        verifyNoMoreInteractions(queue);
        reset(queue);
        mockQueuePrint();
        bot.onEvent(event);
        verify(queue).onResponse(response, event);
    }
}
