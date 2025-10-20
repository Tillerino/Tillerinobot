package tillerino.tillerinobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dagger.Component;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.jupiter.api.Test;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;

public class UserDataManagerTest extends AbstractDatabaseTest {
    @Component(modules = {DockeredMysqlModule.class, TestBackend.Module.class})
    @Singleton
    interface Injector {
        void inject(UserDataManagerTest t);
    }

    {
        DaggerUserDataManagerTest_Injector.create().inject(this);
    }

    @Inject
    UserDataManager manager;

    @Test
    public void testSaveLoad() throws Exception {
        UserData data = manager.loadUserData(534678);
        assertFalse(data.isAllowedToDebug());
        data.setAllowedToDebug(true);
        data.setLastSongInfo(new BeatmapWithMods(123, 456));
        data.close();

        reloadManager();

        data = manager.loadUserData(534678);
        assertTrue(data.isAllowedToDebug());
        assertThat(data.getLastSongInfo()).hasFieldOrPropertyWithValue("beatmap", 123);
    }

    @Test
    public void testV2SaveLoad() throws Exception {
        UserData data = manager.loadUserData(534678);
        assertFalse(data.isV2());
        data.setV2(true);
        data.close();

        reloadManager();

        data = manager.loadUserData(534678);
        assertTrue(data.isV2());
    }

    private void reloadManager() {
        manager = new UserDataManager(null, dbm);
    }

    @Test
    public void testLanguageMutability() throws Exception {
        UserDataManager manager = new UserDataManager(null, dbm);
        List<String> answers = new ArrayList<>();
        try (UserData data = manager.loadUserData(534678)) {
            data.usingLanguage(language -> {
                answers.add(language.apiTimeoutException());
                for (; ; ) {
                    String answer = language.apiTimeoutException();
                    if (answer.equals(answers.get(0))) {
                        assertThat(answers)
                                .size()
                                .as("number of responses to API timeout")
                                .isGreaterThan(1);
                        break;
                    }
                    answers.add(answer);
                }
                return null;
            });
        }

        // at this point we got the first answer again. Time go serialize, deserialize and check if we get the second
        // answer next.
        reloadManager();
        try (UserData data = manager.loadUserData(534678)) {
            data.usingLanguage(lang -> {
                assertThat(lang.apiTimeoutException())
                        .as("API timeout message after reload")
                        .isEqualTo(answers.get(1));
                return null;
            });
        }
    }
}
