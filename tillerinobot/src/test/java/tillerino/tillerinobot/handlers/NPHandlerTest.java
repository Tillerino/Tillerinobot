package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.mockmodules.LiveActivityMockModule;

import dagger.Component;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;

public class NPHandlerTest {
	@Component(modules = { TestBackend.Module.class, LiveActivityMockModule.class })
	@Singleton
	interface Injector {
		void inject(NPHandlerTest t);
	}

	{
		DaggerNPHandlerTest_Injector.create().inject(this);
	}

	@Inject
	NPHandler handler;

	@Test
	public void testMatcher() throws Exception {
		// old style
		assertTrue(NPHandler.npPattern.matcher("is listening to [https://osu.ppy.sh/b/123 title]").find());
		assertTrue(NPHandler.npPattern.matcher("is editing [https://osu.ppy.sh/s/123 title]").find());

		// new style
		assertTrue(NPHandler.npPattern.matcher("is listening to [https://osu.ppy.sh/beatmapsets/361035#osu/955737 title]").find());
		assertTrue(NPHandler.npPattern.matcher("is editing [https://osu.ppy.sh/beatmapsets/361035#osu/955737 title]").find());
	}

	@Test
	public void testOldStyle() throws Exception {
		assertThat(handler.handle("is editing [https://osu.ppy.sh/b/123 title]", null, mock(UserData.class), new Default()))
			.isNotNull()
			.isInstanceOf(GameChatResponse.Success.class);
	}

	@Test
	public void testNewStyle() throws Exception {
		for (String cmd : Arrays.asList(
				"is editing [https://osu.ppy.sh/beatmapsets/312#osu/123 title]",
				// game mode is optional?
				"is listening to [https://osu.ppy.sh/beatmapsets/1158561#2419085 AliA - Kakurenbo]"
				)) {
			assertThat(handler.handle(cmd, null, mock(UserData.class), new Default()))
				.isNotNull()
				.isInstanceOf(GameChatResponse.Success.class);
		}
	}

	@Test
	public void testSetIdOldStyle() throws Exception {
		assertThatThrownBy(() -> handler.handle("is editing [https://osu.ppy.sh/s/123 title]", null, null, new Default()))
			.isInstanceOf(UserException.class)
			.hasMessage(new Default().isSetId());
	}

	@Test
	public void testSetIdNewStyle() throws Exception {
		assertThatThrownBy(() -> handler.handle("is editing [https://osu.ppy.sh/beatmapsets/361035 title]", null, null, new Default()))
		.isInstanceOf(UserException.class)
		.hasMessage(new Default().isSetId());
	}
}
