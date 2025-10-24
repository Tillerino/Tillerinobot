package tillerino.tillerinobot.handlers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.data.PullThrough;

public class DebugHandlerTest {
    PullThrough pullThrough = Mockito.mock(PullThrough.class);

    IrcNameResolver resolver = Mockito.mock(IrcNameResolver.class);

    DebugHandler handler = new DebugHandler(pullThrough, resolver);

    UserData userData = new UserData();

    @BeforeEach
    public void initMocks() {
        userData.setAllowedToDebug(true);
    }

    @Test
    public void testIfHandles() throws Exception {
        assertNotNull(handler.handle("debug resolve bla", null, userData, null));
        assertNotNull(handler.handle("debug getUserByIdFresh 1", null, userData, null));
        assertNotNull(handler.handle("debug getUserByIdCached 1", null, userData, null));
    }
}
