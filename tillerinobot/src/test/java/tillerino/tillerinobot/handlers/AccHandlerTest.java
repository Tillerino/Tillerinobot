package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dagger.Component;
import java.util.regex.Matcher;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.lang.Default;

public class AccHandlerTest extends AbstractDatabaseTest {
    @Component(modules = {TestBackend.Module.class, LiveActivityMockModule.class, DockeredMysqlModule.class})
    @Singleton
    interface Injector {
        void inject(AccHandlerTest t);
    }

    {
        DaggerAccHandlerTest_Injector.create().inject(this);
    }

    @Inject
    UserDataManager userDataManager;

    @Inject
    TestBackend backend;

    @Inject
    AccHandler accHandler;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    UserData userData;

    @BeforeEach
    void setUp() throws Exception {
        backend.hintUser("user", false, 123, 123.0);
        userData = spy(userDataManager.loadUserData(1));
        userData.setLastSongInfo(new BeatmapWithMods(0, 0));
    }

    @Test
    public void testExtendedPattern() {
        assertTrue(AccHandler.extended.matcher("97.2 800x 1m").matches());
    }

    @Test
    public void testExtendedPattern2() {
        Matcher matcher = AccHandler.extended.matcher("97.2% 800x 11m");
        assertTrue(matcher.matches());
        assertEquals("11", matcher.group(3));
    }

    @Test
    public void testSimple() throws Exception {
        assertThat(((Success) accHandler.handle("acc 97.5 800x 1m", null, userData, null)).content())
                .contains("800x");
        verify(diffEstimateProvider).loadBeatmap(eq(0), eq(0L));
    }

    @Test
    public void testLazer() throws Exception {
        userData.setV2(true);
        assertThat(((Success) accHandler.handle("acc 97.5 800x 1m", null, userData, null)).content())
                .contains("800x");
        verify(diffEstimateProvider).loadBeatmap(eq(0), eq(Mods.getMask(Mods.Lazer)));
    }

    @Test
    public void testLargeNumber() throws Exception {
        when(diffEstimateProvider.loadBeatmap(anyInt(), anyLong())).thenReturn(new BeatmapMeta(null, null, null));
        assertThatThrownBy(() -> accHandler.handle("acc 99 80000000000000000000x 1m", null, userData, new Default()))
                .hasMessageContaining("800000000000");
    }

    @Test
    public void testAccTooLow() {
        assertThatThrownBy(() -> accHandler.handle("acc 16.4", null, userData, new Default()))
                .hasMessageContaining("Invalid accuracy");
    }

    @Test
    public void testAccJustHighEnough() throws Exception {
        assertThat(((Success) accHandler.handle("acc 17 800x 1m", null, userData, null)).content())
                .contains("800x");
    }
}
