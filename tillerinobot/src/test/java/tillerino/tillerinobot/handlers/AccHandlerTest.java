package tillerino.tillerinobot.handlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.lang.Language;


public class AccHandlerTest {
	@Test
	public void testExtendedPattern() throws Exception {
		assertTrue(AccHandler.extended.matcher("97.2 800x 1m").matches());
	}
	
	@Test
	public void testSimple() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		when(backend.loadBeatmap(anyInt(), anyLong(), any(Language.class)))
				.thenReturn(new BeatmapMeta(new OsuApiBeatmap(), null, mock(PercentageEstimates.class)));
		AccHandler accHandler = new AccHandler(backend);
		
		IRCBotUser user = mock(IRCBotUser.class);
		UserData userData = mock(UserData.class);
		when(userData.getLastSongInfo()).thenReturn(new BeatmapWithMods(0, 0));
		accHandler.handle("acc 97.5 800x 1m", user, null, userData);
		
		verify(user).message(contains("800x"));
	}
}
