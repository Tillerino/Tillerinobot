package tillerino.tillerinobot;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.LanguageIdentifier;

public class CommandHandlerTest {
	UserData userData = new UserData();

	boolean called_b, called_c;

	@Before
	public void setUp() {
		userData.setLanguage(LanguageIdentifier.Default);
	}

	CommandHandler handler = CommandHandler.handling(
			"A ",
			CommandHandler.alwaysHandling("B", (a, b, c, d) -> called_b = true)
					.or(CommandHandler.alwaysHandling("C",
							(a, b, c, d) -> called_c = true)));

	@Test
	public void testNestedChoices() throws Exception {
		assertEquals("A (B|C)", handler.getChoices());
	}

	@Test
	public void testPass() throws Exception {
		assertFalse(handler.handle("X", null, null, null));
	}

	@Test(expected = UserException.class)
	public void testNoNestedChoice() throws Exception {
		handler.handle("A X", null, null, userData);
	}

	@Test
	public void testB() throws Exception {
		handler.handle("A B", null, null, userData);
		assertTrue(called_b);
		assertFalse(called_c);
	}

	@Test
	public void testC() throws Exception {
		handler.handle("A C", null, null, userData);
		assertTrue(called_c);
		assertFalse(called_b);
	}
}
