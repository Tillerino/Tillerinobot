package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.lang.Default;

class WithHandlerTest extends AbstractDatabaseTest {
    @Component(modules = {TestBackend.Module.class, LiveActivityMockModule.class, DockeredMysqlModule.class})
    @Singleton
    interface Injector {
        void inject(WithHandlerTest t);
    }

    {
        DaggerWithHandlerTest_Injector.create().inject(this);
    }

    @Inject
    UserDataManager userDataManager;

    @Inject
    TestBackend backend;

    @Inject
    WithHandler handler;

    @Inject
    DiffEstimateProvider diffEstimateProvider;

    UserData userData;

    @BeforeEach
    void setUp() throws Exception {
        backend.hintUser("user", false, 123, 123.0);
        userData = spy(userDataManager.loadUserData(1));
        // Set a last song info for the with handler to work with
        userData.setLastSongInfo(new UserDataManager.UserData.BeatmapWithMods(456, 0L));
    }

    @Test
    void testBasicWithCommand() throws Exception {
        assertThat(handler.handle("with HD", null, userData, new Default()))
                .isInstanceOf(GameChatResponse.Message.class);
        verify(diffEstimateProvider).loadBeatmap(eq(456), eq(Mods.getMask(Mods.Hidden)), any());
    }

    @Test
    void testWithLazerForV2User() throws Exception {
        userData.setV2(true);
        assertThat(handler.handle("with HD", null, userData, new Default()))
                .isInstanceOf(GameChatResponse.Message.class);
        verify(diffEstimateProvider).loadBeatmap(eq(456), eq(Mods.getMask(Mods.Hidden, Mods.Lazer)), any());
    }

    @Test
    void testWithMultipleMods() throws Exception {
        assertThat(handler.handle("with HDHR", null, userData, new Default()))
                .isInstanceOf(GameChatResponse.Message.class);
        verify(diffEstimateProvider).loadBeatmap(eq(456), eq(Mods.getMask(Mods.Hidden, Mods.HardRock)), any());
    }

    @Test
    void testMalformedMods() {
        assertThatThrownBy(() -> handler.handle("with invalidmod", null, userData, new Default()))
                .isInstanceOf(UserException.class);
    }

    @Test
    void testNoLastSongInfo() {
        // Clear last song info
        userData.setLastSongInfo(null);
        assertThatThrownBy(() -> handler.handle("with HD", null, userData, new Default()))
                .isInstanceOf(UserException.class);
    }
}
