package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;

@TestModule(TestBackend.Module.class)
public class UserDataManagerTest extends AbstractDatabaseTest {
	@Inject
	UserDataManager manager;

	@Test
	public void testSaveLoad() throws Exception {
		UserData data = manager.getData(534678);
		assertFalse(data.isAllowedToDebug());
		data.setAllowedToDebug(true);
		data.setLastSongInfo(new BeatmapWithMods(123, 456));
		data.close();

		reloadManager();

		data = manager.getData(534678);
		assertTrue(data.isAllowedToDebug());
		assertThat(data.getLastSongInfo()).hasFieldOrPropertyWithValue("beatmap", 123);
	}

	private void reloadManager() {
		reloadEntityManager();
		manager = new UserDataManager(null, em, userDataRepository);
	}

	@Test
	public void testLanguageMutability() throws Exception {
		UserDataManager manager = new UserDataManager(null, em, userDataRepository);
		List<String> answers = new ArrayList<>();
		try(UserData data = manager.getData(534678)) {
			data.usingLanguage(language -> {
				answers.add(language.apiTimeoutException());
				for (;;) {
					String answer = language.apiTimeoutException();
					if (answer.equals(answers.get(0))) {
						assertThat(answers).size().as("number of responses to API timeout").isGreaterThan(1);
						break;
					}
					answers.add(answer);
				}
				return null;
			});
		}

		// at this point we got the first answer again. Time go serialize, deserialize and check if we get the second answer next.
		reloadManager();
		try(UserData data = manager.getData(534678)) {
			data.usingLanguage(lang -> {
				assertThat(lang.apiTimeoutException()).as("API timeout message after reload").isEqualTo(answers.get(1));
				return null;
			});
		}
	}
}
