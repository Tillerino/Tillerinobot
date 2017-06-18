package tillerino.tillerinobot.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.regex.Matcher;

import org.junit.Test;
import org.mockito.internal.matchers.Contains;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.CommandHandler.Success;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;


public class AccHandlerTest {
	@Test
	public void testExtendedPattern() throws Exception {
		assertTrue(AccHandler.extended.matcher("97.2 800x 1m").matches());
	}
	@Test
	public void testExtendedPattern2() throws Exception {
		Matcher matcher = AccHandler.extended.matcher("97.2% 800x 11m");
		assertTrue(matcher.matches());
		assertEquals("11", matcher.group(3));
	}
	
	@Test
	public void testSimple() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		OsuApiBeatmap beatmap = new OsuApiBeatmap();
		beatmap.setMaxCombo(100);
		when(backend.loadBeatmap(anyInt(), anyLong(), any(Language.class)))
				.thenReturn(new BeatmapMeta(beatmap, null, mock(PercentageEstimates.class)));
		AccHandler accHandler = new AccHandler(backend);
		
		UserData userData = mock(UserData.class);
		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));
		assertThat(((Success) accHandler.handle("acc 97.5 800x 1m", null, userData)).getContent(), new Contains("800x"));
	}
	
	@Test(expected=UserException.class)
	public void testLargeNumber() throws Exception {
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(new Default());
		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));

		BotBackend backend = mock(BotBackend.class);
		when(backend.loadBeatmap(anyInt(), anyLong(), any())).thenReturn(new BeatmapMeta(null, null, null));
		try {
			new AccHandler(backend).handle("acc 99 80000000000000000000x 1m", null, userData);
			fail();
		} catch (Exception e) {
			assertThat(e.getMessage(), new Contains("800000000000"));
			throw e;
		}
	}
}
