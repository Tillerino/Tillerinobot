package tillerino.tillerinobot;

import static org.mockito.Mockito.*;

import com.github.omkelderman.sandoku.ProcessorApi;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.WireMockDocker;
import org.tillerino.WireMockDocker.Module;
import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.mockmodules.GameChatClientMockModule;
import org.tillerino.ppaddict.mockmodules.GameChatResponseQueueMockModule;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import org.tillerino.ppaddict.rest.AuthenticationService;
import org.tillerino.ppaddict.util.TestClock;
import tillerino.tillerinobot.data.PullThrough;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.recommendations.RecommendationsManager;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;
import tillerino.tillerinobot.rest.BeatmapsService;
import tillerino.tillerinobot.rest.BotApiRule;

public abstract class TestBase extends AbstractDatabaseTest {
    @Inject
    protected IRCBot bot;

    @Inject
    protected BeatmapsService beatmapsService;

    @Inject
    protected GameChatClient gameChatClient;

    @Inject
    protected AuthenticationService authenticationService;

    @Inject
    GameChatResponseQueue queue;

    @dagger.Component(
            modules = {
                DockeredMysqlModule.class,
                TestBaseModule.class,
                Module.class,
                LiveActivityMockModule.class,
                TestClock.Module.class,
                GameChatResponseQueueMockModule.class,
                GameChatClientMockModule.class
            })
    @Singleton
    interface Injector {
        void inject(TestBase t);
    }

    {
        DaggerTestBase_Injector.create().inject(this);
    }

    @Inject
    protected OsuApi osuApi;

    @Inject
    protected OsuApiV1 osuApiV1;

    @Inject
    protected OsuApiV2 osuApiV2;

    @Inject
    protected AuthenticationService auth;

    @Inject
    protected BeatmapDownloader beatmapDownloader;

    @Inject
    protected BeatmapsLoader beatmapsLoader;

    @Inject
    protected DiffEstimateProvider diffEstimateProvider;

    @Inject
    protected ProcessorApi sanDoku;

    @Inject
    protected BotBackend backend;

    @Inject
    protected RecommendationsManager recommendationsManager;

    @Inject
    protected Recommender recommender;

    @Named("standard")
    @Inject
    protected Recommender standardRecommender;

    @Inject
    protected PullThrough pullThrough;

    @Inject
    protected IrcNameResolver ircNameResolver;

    @Inject
    protected TestClock clock;

    @Inject
    protected UserDataManager userDataManager;

    @Inject
    protected LiveActivity liveActivity;

    @Inject
    protected PlayerService playerService;

    @Inject
    protected OsutrackDownloader osutrackDownloader;

    @RegisterExtension
    public final WireMockDocker wireMock = new WireMockDocker();

    /** Assign this to a field in your test annotated with {@link RegisterExtension} to start the API locally. */
    @Inject
    public BotApiRule botApiNoInit;

    public static void mockApiBeatmaps(BeatmapsLoader beatmapsLoader) throws Exception {
        doAnswer(inv -> MockData.createMockBeatmap(inv.getArgument(0)))
                .when(beatmapsLoader)
                .getBeatmap(anyInt(), anyLong());
    }

    public static void mockBeatmapMetas(DiffEstimateProvider diffEstimateProvider) throws Exception {
        doAnswer(inv -> MockData.createMockBeatmapMeta(inv.getArgument(0), inv.getArgument(1)))
                .when(diffEstimateProvider)
                .loadBeatmap(anyInt(), anyLong());
    }

    public static void mockRecommendations(Recommender recommender) throws Exception {
        doAnswer(inv -> MockData.createMockRecommendations(inv.getArgument(0), inv.getArgument(3), inv.getArgument(4)))
                .when(recommender)
                .loadRecommendations(any(), any(), any(), anyBoolean(), anyLong());
    }
}
