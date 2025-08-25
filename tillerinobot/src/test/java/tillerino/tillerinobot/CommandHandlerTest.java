package tillerino.tillerinobot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.ppaddict.chat.GameChatResponse;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.LanguageIdentifier;

public class CommandHandlerTest {
	UserData userData = new UserData();

	boolean called_b, called_c;

	@BeforeEach
	public void setUp() {
		userData.setLanguage(LanguageIdentifier.Default);
	}

	CommandHandler handler = CommandHandler.handling(
			"A ",
			CommandHandler.alwaysHandling("B", (a, c, d, lang) -> { called_b = true; return GameChatResponse.none(); })
					.or(CommandHandler.alwaysHandling("C",
							(a, c, d, lang) -> { called_c = true; return GameChatResponse.none(); })));

	@Test
	public void testNestedChoices() throws Exception {
		assertEquals("A (B|C)", handler.getChoices());
	}

	@Test
	public void testPass() throws Exception {
		assertNull(handler.handle("X", null, null, null));
	}

	@Test
	public void testNoNestedChoice() throws Exception {
		assertThrows(UserException.class, () -> handler.handle("A X", null, userData, new Default()));
	}

	@Test
	public void testB() throws Exception {
		handler.handle("A B", null, userData, null);
		assertTrue(called_b);
		assertFalse(called_c);
	}

	@Test
	public void testC() throws Exception {
		handler.handle("A C", null, userData, null);
		assertTrue(called_c);
		assertFalse(called_b);
	}
}
