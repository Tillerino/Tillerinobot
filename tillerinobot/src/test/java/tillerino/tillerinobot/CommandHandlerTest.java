package tillerino.tillerinobot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import tillerino.tillerinobot.CommandHandler.NoResponse;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.LanguageIdentifier;

public class CommandHandlerTest {
	UserData userData = new UserData();

	boolean called_b, called_c;

	@Before
	public void setUp() {
		userData.setLanguage(LanguageIdentifier.Default);
	}

	CommandHandler handler = CommandHandler.handling(
			"A ",
			CommandHandler.alwaysHandling("B", (a, c, d) -> { called_b = true; return new NoResponse(); })
					.or(CommandHandler.alwaysHandling("C",
							(a, c, d) -> { called_c = true; return new NoResponse(); })));

	@Test
	public void testNestedChoices() throws Exception {
		assertEquals("A (B|C)", handler.getChoices());
	}

	@Test
	public void testPass() throws Exception {
		assertNull(handler.handle("X", null, null));
	}

	@Test(expected = UserException.class)
	public void testNoNestedChoice() throws Exception {
		handler.handle("A X", null, userData);
	}

	@Test
	public void testB() throws Exception {
		handler.handle("A B", null, userData);
		assertTrue(called_b);
		assertFalse(called_c);
	}

	@Test
	public void testC() throws Exception {
		handler.handle("A C", null, userData);
		assertTrue(called_c);
		assertFalse(called_b);
	}
}
