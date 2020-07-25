package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.web.AbstractPpaddictUserDataService;

import tillerino.tillerinobot.BotBackend;

@RunWith(MockitoJUnitRunner.class)
public class LinkPpaddictHandlerTest {
	@Mock
	AbstractPpaddictUserDataService ppaddictService;

	@Mock
	BotBackend backend;

	@InjectMocks
	LinkPpaddictHandler handler;

	@Before
	public void setup() {
		when(ppaddictService.tryLinkToPpaddict(any(), anyInt())).thenReturn(Optional.empty());
		when(backend.tryLinkToPatreon(anyString(), any())).thenReturn(null);
	}

	@Test
	public void testHandle() throws Exception {
		assertThatThrownBy(() -> handler.handle("12345678901234567890123456789012", user(12345, "usr"), null))
				.hasMessage("nothing happened.");
		verify(ppaddictService).tryLinkToPpaddict("12345678901234567890123456789012", 12345);
		verify(backend).tryLinkToPatreon(eq("12345678901234567890123456789012"), argThat(u -> u.getUserId() == 12345));
	}

	@Test
	public void testPpaddict() throws Exception {
		doReturn(Optional.of("yeah")).when(ppaddictService).tryLinkToPpaddict("12345678901234567890123456789012", 12345);
		assertThat(handler.handle("12345678901234567890123456789012", user(12345, "usr"), null))
				.isInstanceOfSatisfying(Success.class, s -> assertThat((Object) s).hasFieldOrPropertyWithValue("content", "linked to yeah"));
		verify(ppaddictService).tryLinkToPpaddict("12345678901234567890123456789012", 12345);
	}

	@Test
	public void testPatreon() throws Exception {
		doReturn("yeah").when(backend).tryLinkToPatreon("12345678901234567890123456789012", user(12345, "usr"));
		assertThat(handler.handle("12345678901234567890123456789012", user(12345, "usr"), null))
		.isInstanceOfSatisfying(Success.class, s -> assertThat((Object) s).hasFieldOrPropertyWithValue("content", "linked to yeah"));
		verify(backend).tryLinkToPatreon(eq("12345678901234567890123456789012"), argThat(u -> u.getUserId() == 12345));
	}

	@Test
	public void noMatch() throws Exception {
		assertThat(handler.handle("fgds", null, null)).isNull();
	}

	OsuApiUser user(@UserId int id, @OsuName String name) {
		OsuApiUser user = new OsuApiUser();
		user.setUserId(id);
		user.setUserName(name);
		return user;
	}
}
