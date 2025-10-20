package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import tillerino.tillerinobot.BotBackend;

public class LinkPpaddictHandlerTest {
    BotBackend backend = mock(BotBackend.class);

    LinkPpaddictHandler handler = new LinkPpaddictHandler(backend);

    @BeforeEach
    public void setup() {
        when(backend.tryLinkToPatreon(anyString(), any())).thenReturn(null);
    }

    @Test
    public void testHandle() throws Exception {
        assertThatThrownBy(() -> handler.handle("12345678901234567890123456789012", user(12345, "usr"), null, null))
                .hasMessage("nothing happened.");
        verify(backend).tryLinkToPatreon(eq("12345678901234567890123456789012"), argThat(u -> u.getUserId() == 12345));
    }

    @Test
    public void testPatreon() throws Exception {
        doReturn("yeah").when(backend).tryLinkToPatreon("12345678901234567890123456789012", user(12345, "usr"));
        assertThat(handler.handle("12345678901234567890123456789012", user(12345, "usr"), null, null))
                .isInstanceOfSatisfying(Success.class, s -> assertThat((Object) s)
                        .hasFieldOrPropertyWithValue("content", "linked to yeah"));
        verify(backend).tryLinkToPatreon(eq("12345678901234567890123456789012"), argThat(u -> u.getUserId() == 12345));
    }

    @Test
    public void noMatch() throws Exception {
        assertThat(handler.handle("fgds", null, null, null)).isNull();
    }

    @Test
    public void testPattern() throws Exception {
        assertTrue(LinkPpaddictHandler.TOKEN_PATTERN
                .matcher(LinkPpaddictHandler.newKey())
                .matches());
    }

    OsuApiUser user(@UserId int id, @OsuName String name) {
        OsuApiUser user = new OsuApiUser();
        user.setUserId(id);
        user.setUserName(name);
        return user;
    }
}
