package tillerino.tillerinobot.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.LiveActivity;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;

public class AccHandlerTest {
	BotBackend backend = mock(BotBackend.class);
	UserData userData = mock(UserData.class);
	PercentageEstimates percentageEstimates = mock(PercentageEstimates.class);
	LiveActivity liveActivity = mock(LiveActivity.class);
	OsuApiBeatmap beatmap = new OsuApiBeatmap();

	@Before
	public void setup() throws Exception {
		when(backend.loadBeatmap(anyInt(), anyLong(), any()))
			.thenReturn(new BeatmapMeta(beatmap, null, percentageEstimates));
	}

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
		beatmap.setMaxCombo(100);
		AccHandler accHandler = new AccHandler(backend, liveActivity);

		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));
		assertThat(((Success) accHandler.handle("acc 97.5 800x 1m", null, userData, null)).content())
				.contains("800x");
	}

	@Test
	public void testLargeNumber() throws Exception {
		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));

		when(backend.loadBeatmap(anyInt(), anyLong(), any())).thenReturn(new BeatmapMeta(null, null, null));
		assertThatThrownBy(() -> new AccHandler(backend, liveActivity)
				.handle("acc 99 80000000000000000000x 1m", null, userData, new Default()))
						.hasMessageContaining("800000000000");
	}

	@Test
	public void testAccTooLow() throws Exception {
		beatmap.setMaxCombo(100);
		AccHandler accHandler = new AccHandler(backend, liveActivity);

		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));
		assertThatThrownBy(() -> accHandler.handle("acc 16.4", null, userData, new Default()))
				.hasMessageContaining("Invalid accuracy");
	}

	@Test
	public void testAccJustHighEnough() throws Exception {
		beatmap.setMaxCombo(100);
		AccHandler accHandler = new AccHandler(backend, liveActivity);

		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));
		assertThat(((Success) accHandler.handle("acc 17 800x 1m", null, userData, null)).content())
			.contains("800x");
	}
}
