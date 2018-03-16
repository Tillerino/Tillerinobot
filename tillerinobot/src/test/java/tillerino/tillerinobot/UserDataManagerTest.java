package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.lang.Language;

public class UserDataManagerTest extends AbstractDatabaseTest {
	UserDataManager manager;

	@Before
	public void createManager() {
		manager = new UserDataManager(null, emf, em, userDataRepository);
	}

	@Test
	public void testSaveLoad() throws Exception {
		UserData data = manager.getData(534678);
		assertFalse(data.isAllowedToDebug());
		data.setAllowedToDebug(true);

		reloadManager();

		data = manager.getData(534678);
		assertTrue(data.isAllowedToDebug());
		manager.tidyUp(false);
	}

	private void reloadManager() {
		manager.tidyUp(false);
		
		em.close();
		em.setThreadLocalEntityManager(emf.createEntityManager());
		manager = new UserDataManager(null, emf, em, userDataRepository);
	}

	@Test
	public void testLanguageMutability() throws Exception {
		UserDataManager manager = new UserDataManager(null, emf, em, userDataRepository);
		UserData data = manager.getData(534678);
		Language language = data.getLanguage();
		List<String> answers = new ArrayList<>();
		answers.add(language.apiTimeoutException());
		for (;;) {
			String answer = language.apiTimeoutException();
			if (answer.equals(answers.get(0))) {
				assertThat(answers).size().as("number of responses to API timeout").isGreaterThan(1);
				break;
			}
			answers.add(answer);
		}
		// at this point we got the first answer again. Time go serialize, deserialize and check if we get the second answer next.
		reloadManager();
		data = manager.getData(534678);
		assertThat(data.getLanguage().apiTimeoutException()).as("API timeout message after reload").isEqualTo(answers.get(1));
	}
}
