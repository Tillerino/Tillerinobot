package org.tillerino.ppaddict;

import static org.tillerino.osuApiModel.Mods.DoubleTime;
import static org.tillerino.osuApiModel.Mods.HardRock;
import static org.tillerino.osuApiModel.Mods.Hidden;
import static org.tillerino.osuApiModel.Mods.getMask;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.auth.Credentials;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.DiffEstimateProvider;

/**
 * Backend test implementation. Not thread safe or anything. Serializes database to ppaddict-db.json file in working
 * directory on every write for easy debugging. Delete the file and restart the app to reset the database.
 *
 * @author Tillerino
 */
@Singleton
public class TestBackend implements PpaddictBackend {
    private final DiffEstimateProvider diffEstimateProvider;

    @Inject
    public TestBackend(DiffEstimateProvider diffEstimateProvider) {
        this.diffEstimateProvider = diffEstimateProvider;

        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream("ppaddict-db.json")))) {
            database = new ObjectMapper().readValue(reader, Database.class);
        } catch (IOException e) {
            // that's okay
        }
    }

    void writeDatabase() {
        File file = new File("ppaddict-db.json");
        System.out.println(file.getAbsolutePath());
        try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
            new ObjectMapper().writeValue(writer, database);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Database {
        public final Map<String, Credentials> cookies = new HashMap<>();

        public Map<String, PersistentUserData> userData = new HashMap<>();

        public Map<String, String> forward = new HashMap<>();
    }

    Database database = new Database();

    @Override
    public Credentials resolveCookie(String cookie) {
        return database.cookies.get(cookie);
    }

    @Override
    public String createCookie(Credentials userIdentifier) {
        String cookie = database.cookies.size() + "-cohkay";
        database.cookies.put(cookie, userIdentifier);
        writeDatabase();
        return cookie;
    }

    @Override
    public Map<BeatmapWithMods, BeatmapData> getBeatmaps() {
        HashMap<BeatmapWithMods, BeatmapData> ret = new HashMap<>();

        for (Integer id : tillerino.tillerinobot.TestBackend.getSetIds().keySet()) {
            for (long mods : new long[] {0, getMask(Hidden, HardRock), getMask(DoubleTime)}) {
                try {
                    final BeatmapMeta meta = diffEstimateProvider.loadBeatmap(id, mods);
                    ret.put(
                            new BeatmapWithMods(id, mods),
                            new BeatmapData(
                                    meta.getEstimates(),
                                    OsuApiBeatmapForPpaddict.Mapper.INSTANCE.shrink(meta.getBeatmap())));
                } catch (SQLException | IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return ret;
    }

    @Override
    public long getBeatmapsGeneration() {
        return 0;
    }
}
