package tillerino.tillerinobot.handlers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.RecommendationsManager;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.LanguageIdentifier;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;


public class OptionsHandlerTest {
	enum E {
		AA,
		BB
	}

	@Mock
	UserData userData;

	@Before
	public void setUp() {
	  MockitoAnnotations.initMocks(this);
      when(userData.getLanguage()).thenReturn(mock(Language.class));
	}

	@Test
	public void test() throws Exception {
		assertEquals(E.AA, OptionsHandler.find(E.values(), "a"));
		assertEquals(E.AA, OptionsHandler.find(E.values(), "ac"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), "b"));
		assertEquals(E.BB, OptionsHandler.find(E.values(), "cb"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEmpty() throws Exception {
		// this will match nothing
		OptionsHandler.find(E.values(), "");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCenter() throws Exception {
		// this will match both AA and BB
		OptionsHandler.find(E.values(), "ab");
	}
	
	@Test
	public void testSetLanguage() throws Exception {
		OptionsHandler handler = new OptionsHandler(null);
		
		handler.handle("set languge tsundre", null, userData);
		
		verify(userData).setLanguage(LanguageIdentifier.Tsundere);
	}
	
	@Test(expected=UserException.class)
	public void testSetUnknownLanguage() throws Exception {
		OptionsHandler handler = new OptionsHandler(null);
		
		handler.handle("set language defflt", null, userData);
	}

    @Test
    public void testDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(new RecommendationsManager(mock(BotBackend.class), null, null));

      handler.handle("set default hd hr", null, userData);

      verify(userData).setDefaultRecommendationOptions("hd hr");
    }

    @Test
    public void testClearDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(null);

      handler.handle("set default", null, userData);

      verify(userData).setDefaultRecommendationOptions(null);
    }

    @Test(expected = UserException.class)
    public void testInvalidDefaultSettings() throws Exception {
      OptionsHandler handler = new OptionsHandler(new RecommendationsManager(mock(BotBackend.class), null, null));

      handler.handle("set default invalid", null, userData);
    }
}
