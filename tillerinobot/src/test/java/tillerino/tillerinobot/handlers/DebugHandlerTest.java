package tillerino.tillerinobot.handlers;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;

public class DebugHandlerTest {
	@Mock
	BotBackend backend;
	
	@Mock
	IrcNameResolver resolver;
	
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
		assertNotNull(handler.handle("debug resolve bla", null, userData));
		assertNotNull(handler.handle("debug getUserByIdFresh 1", null, userData));
		assertNotNull(handler.handle("debug getUserByIdCached 1", null, userData));
	}
}
