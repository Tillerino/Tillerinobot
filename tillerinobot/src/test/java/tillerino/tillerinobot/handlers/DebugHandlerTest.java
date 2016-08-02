package tillerino.tillerinobot.handlers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;

public class DebugHandlerTest {
	@Mock
	BotBackend backend;
	
	@Mock
	IrcNameResolver resolver;
	
	@Mock
	IRCBotUser ircBotUser;
	
	DebugHandler handler;
	
	UserData userData = new UserData();
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		
		handler = new DebugHandler(backend, resolver);
		userData.setAllowedToDebug(true);
	}
	
	@Test
	public void testIfHandles() throws Exception {
		assertTrue(handler.handle("debug resolve bla", ircBotUser, null, userData));
		assertTrue(handler.handle("debug getUserByIdFresh 1", ircBotUser, null, userData));
		assertTrue(handler.handle("debug getUserByIdCached 1", ircBotUser, null, userData));
	}
}
