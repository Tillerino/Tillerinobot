package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.ppaddict.chat.GameChatResponse;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.Default;

class NPHandlerTest extends TestBase {
    NPHandler handler = new NPHandler(liveActivity, diffEstimateProvider);

    UserData userData;

    @BeforeEach
    void setUp() throws Exception {
        MockData.mockUser("user", false, 123, 123.0, 123, backend, osuApi, standardRecommender);
        mockBeatmapMetas(diffEstimateProvider);
        userData = spy(userDataManager.loadUserData(1));
    }

    @Test
    void testMatcher() {
        // old style
        assertTrue(NPHandler.npPattern
                .matcher("is listening to [https://osu.ppy.sh/b/123 title]")
                .find());
        assertTrue(NPHandler.npPattern
                .matcher("is editing [https://osu.ppy.sh/s/123 title]")
                .find());

        // new style
        assertTrue(NPHandler.npPattern
                .matcher("is listening to [https://osu.ppy.sh/beatmapsets/361035#osu/955737 title]")
                .find());
        assertTrue(NPHandler.npPattern
                .matcher("is editing [https://osu.ppy.sh/beatmapsets/361035#osu/955737 title]")
                .find());
    }

    @Test
    void testOldStyle() throws Exception {
        assertThat(handler.handle("is editing [https://osu.ppy.sh/b/123 title]", null, userData, new Default()))
                .isInstanceOf(GameChatResponse.Success.class);
    }

    @Test
    void testNewStyleEditing() throws Exception {
        assertThat(handler.handle(
                        "is editing [https://osu.ppy.sh/beatmapsets/312#osu/123 title]", null, userData, new Default()))
                .isInstanceOf(GameChatResponse.Success.class);
        verify(diffEstimateProvider).loadBeatmap(eq(123), eq(0L));
    }

    @Test
    void testNewStyleListening() throws Exception {
        // game mode is optional?
        assertThat(handler.handle(
                        "is listening to [https://osu.ppy.sh/beatmapsets/1158561#2419085 AliA - Kakurenbo]",
                        null,
                        userData,
                        new Default()))
                .isInstanceOf(GameChatResponse.Success.class);
        verify(diffEstimateProvider).loadBeatmap(eq(2419085), eq(0L));
    }

    @Test
    void testSetIdOldStyle() {
        assertThatThrownBy(() ->
                        handler.handle("is editing [https://osu.ppy.sh/s/123 title]", null, userData, new Default()))
                .isInstanceOf(UserException.class)
                .hasMessage(new Default().isSetId());
    }

    @Test
    void testSetIdNewStyle() {
        assertThatThrownBy(() -> handler.handle(
                        "is editing [https://osu.ppy.sh/beatmapsets/361035 title]", null, userData, new Default()))
                .isInstanceOf(UserException.class)
                .hasMessage(new Default().isSetId());
    }

    @Test
    void testLazerNp() throws Exception {
        userData.setV2(true);
        assertThat(handler.handle(
                        "is listening to [https://osu.ppy.sh/beatmapsets/361035#osu/955737 title]",
                        null,
                        userData,
                        new Default()))
                .isInstanceOf(GameChatResponse.Success.class);
        verify(diffEstimateProvider).loadBeatmap(eq(955737), eq(Mods.getMask(Mods.Lazer)));
    }
}
