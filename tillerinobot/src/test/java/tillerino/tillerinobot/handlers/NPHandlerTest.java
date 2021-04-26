package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;

@RunWith(InjectionRunner.class)
@TestModule(value = { TestBackend.Module.class }, mocks = LiveActivity.class)
public class NPHandlerTest {
	@Inject
	NPHandler handler;

	@Test
	public void testMatcher() throws Exception {
		assertTrue(NPHandler.npPattern.matcher("is listening to [https://osu.ppy.sh/b/123 title]").find());
		assertTrue(NPHandler.npPattern.matcher("is editing [https://osu.ppy.sh/s/123 title]").find());
	}

	@Test
	public void testNp() throws Exception {
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(new Default());
		assertThat(handler.handle("is editing [https://osu.ppy.sh/b/123 title]", null, userData))
			.isNotNull()
			.isInstanceOf(GameChatResponse.Success.class);
	}

	@Test
	public void testSetId() throws Exception {
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(new Default());
		assertThatThrownBy(() -> handler.handle("is editing [https://osu.ppy.sh/s/123 title]", null, userData))
			.isInstanceOf(UserException.class)
			.hasMessage(new Default().isSetId());
	}
}
