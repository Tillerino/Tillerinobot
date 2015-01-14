package tillerino.tillerinobot.handlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.LanguageIdentifier;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Language;


public class OptionsHandlerTest {
	enum E {
		AA,
		BB
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
		OptionsHandler handler = new OptionsHandler();
		
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(mock(Language.class));
		
		handler.handle("set languge tsundre", null, null, userData);
		
		verify(userData).setLanguage(LanguageIdentifier.Tsundere);
	}
	
	@Test(expected=UserException.class)
	public void testSetUnknownLanguage() throws Exception {
		OptionsHandler handler = new OptionsHandler();
		
		UserData userData = mock(UserData.class);
		when(userData.getLanguage()).thenReturn(mock(Language.class));
		
		handler.handle("set language defflt", null, null, userData);
	}
}
