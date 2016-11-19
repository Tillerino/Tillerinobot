package tillerino.tillerinobot;

import static org.junit.Assert.*;

import org.junit.Test;

import tillerino.tillerinobot.UserDataManager.UserData;

public class UserDataManagerTest extends AbstractDatabaseTest {
	@Test
	public void testSaveLoad() throws Exception {
		UserDataManager manager = new UserDataManager(null, emf, em, userDataRepository);
		
		UserData data = manager.getData(534678);
		assertFalse(data.isAllowedToDebug());
		data.setAllowedToDebug(true);
		manager.tidyUp(false);
		
		em.close();
		em.setThreadLocalEntityManager(emf.createEntityManager());
		manager = new UserDataManager(null, emf, em, userDataRepository);
		data = manager.getData(534678);
		assertTrue(data.isAllowedToDebug());
		manager.tidyUp(false);
	}
}
