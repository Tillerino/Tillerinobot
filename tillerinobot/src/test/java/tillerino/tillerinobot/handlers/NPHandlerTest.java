package tillerino.tillerinobot.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.tillerino.ppaddict.chat.LiveActivity;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;


public class NPHandlerTest {
	@Test
	public void testMatcher() throws Exception {
		assertTrue(NPHandler.npPattern.matcher("is listening to [https://osu.ppy.sh/b/123 title]").find());
		assertTrue(NPHandler.npPattern.matcher("is editing [https://osu.ppy.sh/s/123 title]").find());
	}
	
	@Test
	public void testSetId() throws Exception {
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(new Default());
		try {
			new NPHandler(null, mock(LiveActivity.class)).handle("is editing [https://osu.ppy.sh/s/123 title]", null, userData);
			fail();
		} catch(UserException e) {
			assertEquals(new Default().isSetId(), e.getMessage());
		}
	}
}
