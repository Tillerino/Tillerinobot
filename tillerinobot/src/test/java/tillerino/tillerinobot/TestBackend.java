package tillerino.tillerinobot;

import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.util.MaintenanceException;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.diff.*;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Model;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.recommendations.TopPlay;

/**
 * Backend implementation for the purposes of testing the Frontend.
 *
 * <p>Everything that the backend does, is saved in the file tillerinobot-db.json in the current working directory. You
 * can edit the file if you want to test something specific.
 *
 * <p>Beatmaps are randomly generated while trying to look realistic (consistency between star diff, version, and pp).
 * The pp curve is approximates with acc^5.
 *
 * <p>Recommendations just look for the closest candidates to (user's pp/20) with 98% acc while respecting selected
 * mods.
 *
 * @author Tillerino
 */
@Singleton
public class TestBackend implements BotBackend {
    @JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE)
    static class User {
        int lastVisistedVersion = 0;
        ApiUser apiUser;
        boolean isDonator = false;
        long lastActivity;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.NON_PRIVATE)
    static class Database {
        Map<Integer, User> users = new HashMap<>();

        Map<String, Integer> userNames = new HashMap<>();
    }

    private static final ObjectMapper JACKSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    boolean serialize;

    BeatmapsLoader loader;

    @Getter
    static Map<Integer, Integer> setIds = new HashMap<>();

    static {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(TestBackend.class.getResourceAsStream("/beatmapIds.txt")))) {
            for (String line; (line = reader.readLine()) != null; ) {
                String[] s = line.split("\t");
                setIds.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Inject
    public TestBackend(@Named("tillerinobot.test.persistentBackend") boolean serialize, BeatmapsLoader loader) {
        this.serialize = serialize;
        this.loader = loader;
        if (serialize) {
            try (Reader reader =
                    new InputStreamReader(new BufferedInputStream(new FileInputStream("tillerinobot-db.json")))) {
                database = JACKSON.readValue(reader, Database.class);
            } catch (FileNotFoundException e) {
                System.err.println("Could not load existing database: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void writeDatabase() {
        if (!serialize) return;

        try (Writer writer =
                new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("tillerinobot-db.json")))) {
            JACKSON.writeValue(writer, database);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Database database = new Database();

    @Override
    public BeatmapMeta loadBeatmap(int beatmapid, final long mods, Language lang) throws SQLException, IOException {
        OsuApiBeatmap beatmap = loader.getBeatmap(beatmapid, 0L);

        BeatmapImpl cBeatmap = BeatmapImpl.builder()
                .modsUsed(DiffEstimateProvider.getDiffMods(mods))
                .StarDiff((float) beatmap.getStarDifficulty())
                .AimDifficulty((float) beatmap.getStarDifficulty() / 2)
                .SpeedDifficulty((float) beatmap.getStarDifficulty() / 2)
                .SliderFactor(1f)
                .ApproachRate((float) beatmap.getApproachRate())
                .OverallDifficulty((float) beatmap.getOverallDifficulty())
                .MaxCombo(beatmap.getMaxCombo())
                .HitCircleCount(200)
                .SpinnerCount(10)
                .build();
        PercentageEstimates estimates = new PercentageEstimatesImpl(cBeatmap, mods);

        return new BeatmapMeta(beatmap, null, estimates);
    }

    public void hintUser(String username, boolean isDonator, int rank, double pp) {
        hintUser(username, isDonator, rank, pp, database.userNames.size() + 1);
    }

    public void hintUser(String username, boolean isDonator, int rank, double pp, int userid) {
        User user;
        boolean write = false;
        if (!database.userNames.containsKey(username)) {
            write = true;
            user = new User();
            user.apiUser = new ApiUser();
            user.apiUser.setUserId(userid);
            database.userNames.put(username, userid);
            database.users.put(userid, user);
        }

        user = database.users.get(database.userNames.get(username));

        user.isDonator = isDonator;
        user.apiUser.setUserName(username);
        user.apiUser.setPp(pp);
        user.apiUser.setRank(rank);

        if (write) {
            writeDatabase();
        }
    }

    @Override
    public int getLastVisitedVersion(String nick) throws SQLException, UserException {
        return database.users.get(database.userNames.get(nick)).lastVisistedVersion;
    }

    @Override
    public void setLastVisitedVersion(String nick, int version) throws SQLException {
        database.users.get(database.userNames.get(nick)).lastVisistedVersion = version;
        writeDatabase();
    }

    @Override
    public ApiUser getUser(int userid, long maxAge) throws SQLException, IOException {
        User user = database.users.get(userid);
        return user.apiUser;
    }

    @Override
    public void registerActivity(int userid, long timestamp) throws SQLException {
        database.users.get(userid).lastActivity = timestamp;
        writeDatabase();
    }

    @Override
    public long getLastActivity(OsuApiUser user) throws SQLException {
        return database.users.get(user.getUserId()).lastActivity;
    }

    @Override
    public int getDonator(int user) throws SQLException, IOException {
        return database.users.get(user).isDonator ? 1 : 0;
    }

    private List<BeatmapMeta> findBeatmaps(final double equivalentPp, long requestMods, boolean nomod)
            throws SQLException, IOException {
        ArrayList<Long> mods = new ArrayList<>();
        if (requestMods == 0) {
            mods.add(0l);
            if (!nomod) {
                mods.add(Mods.getMask(Mods.DoubleTime));
                mods.add(Mods.getMask(Mods.DoubleTime, Mods.Hidden));
                mods.add(Mods.getMask(Mods.HardRock));
                mods.add(Mods.getMask(Mods.Hidden, Mods.HardRock));
            }
        } else {
            mods.add(requestMods);
            mods.add(requestMods | Mods.getMask(Mods.Hidden));
        }
        List<BeatmapMeta> maps = new ArrayList<>();
        for (int i : setIds.keySet()) {
            for (long m : mods) {
                BeatmapMeta meta = loadBeatmap(i, m, null);
                if (Math.abs(1 - meta.getEstimates().getPP(.98) / equivalentPp) < .15) {
                    maps.add(meta);
                }
                meta.setPersonalPP((int) meta.getEstimates().getPP(.98));
            }
        }
        return maps;
    }

    @Override
    public String tryLinkToPatreon(String token, OsuApiUser user) {
        return null;
    }

    @Override
    public List<ApiScore> getRecentPlays(int userid) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public ApiUser downloadUser(String userName) throws IOException, SQLException {
        Integer userid = database.userNames.get(userName);
        if (userid == null) {
            return null;
        }
        return database.users.get(userid).apiUser;
    }

    @Singleton
    @NoArgsConstructor(onConstructor_ = @Inject)
    public static class TestBeatmapsLoader implements BeatmapsLoader {
        @Override
        public OsuApiBeatmap getBeatmap(int beatmapid, long mods) {
            Random rand = new Random(beatmapid);
            OsuApiBeatmap beatmap = new OsuApiBeatmap();
            beatmap.setBeatmapId(beatmapid);
            if (setIds.containsKey(beatmapid)) {
                beatmap.setSetId(setIds.get(beatmapid));
            }
            {
                // ARTIST
                String[] artists = {"Hatsune Miku", "IOSYS", "Nightcore", "DragonForce", "ClariS"};
                beatmap.setArtist(artists[beatmapid % artists.length]);
            }
            beatmap.setTitle("Beatmap " + beatmapid);
            {
                // VERSION AND DIFFICULTY
                String[] versions = {"Easy", "Normal", "Hard", "Hyper", "Insane", "Another", "Extra"};
                int diff = beatmapid % versions.length;
                beatmap.setVersion(versions[diff]);

                beatmap.setStarDifficulty(diff + rand.nextDouble());
                beatmap.setTotalLength((int) (30 + Math.pow(rand.nextDouble(), 3) * 600));
                beatmap.setApproachRate(5 + Math.min(4, diff) + (int) (rand.nextDouble() + .5));
                beatmap.setCircleSize(diff + 1);
                beatmap.setBpm(50 * Math.pow(2, diff * .4 + rand.nextDouble()));
                beatmap.setMaxCombo(100);
            }
            return beatmap;
        }
    }

    @RequiredArgsConstructor(onConstructor = @__(@Inject))
    public static class TestRecommender implements Recommender {
        private final TestBackend backend;

        @Override
        public List<TopPlay> loadTopPlays(int userId) throws SQLException, MaintenanceException, IOException {
            OsuApiUser user = backend.getUser(userId, 0);
            final double equivalent = user.getPp() / 20;
            List<BeatmapMeta> maps = backend.findBeatmaps(equivalent, 0, false);
            List<TopPlay> plays = new ArrayList<>();
            for (int i = 0; i < maps.size() && i < 50; i++) {
                BeatmapMeta meta = maps.get(i);
                plays.add(
                        new TopPlay(userId, i, meta.getBeatmap().getBeatmapId(), meta.getMods(), meta.getPersonalPP()));
            }
            return plays;
        }

        @Override
        public Collection<BareRecommendation> loadRecommendations(
                List<TopPlay> topPlays, Collection<Integer> exclude, Model model, boolean nomod, long requestMods)
                throws SQLException, IOException, UserException {
            double equivalent = equivalentPp(topPlays);
            List<BeatmapMeta> maps = backend.findBeatmaps(equivalent, requestMods, nomod);
            Collection<BareRecommendation> recommendations = new ArrayList<>();
            for (final BeatmapMeta meta : maps) {
                double _98percentPp = meta.getEstimates().getPP(.98);
                recommendations.add(new BareRecommendation(
                        meta.getBeatmap().getBeatmapId(),
                        meta.getMods(),
                        new long[0],
                        (int) Math.ceil((_98percentPp + equivalent) / 2),
                        .15 - Math.abs(1 - _98percentPp / equivalent)));
            }
            return recommendations;
        }

        double equivalentPp(List<TopPlay> plays) {
            plays = new ArrayList<>(plays);
            Collections.sort(plays, Comparator.comparingDouble(TopPlay::getPp).reversed());
            double ppSum = 0;
            double partialSum = 0;

            for (int i = 0; i < plays.size(); i++) {
                partialSum += Math.pow(.95, i);
                ppSum += plays.get(i).getPp() * Math.pow(.95, i);
            }

            return ppSum / partialSum;
        }
    }

    @dagger.Module
    public interface Module {
        @dagger.Provides
        @Singleton
        static TestBackend testBackend(BeatmapsLoader loader) {
            return spy(new TestBackend(false, loader));
        }

        @dagger.Provides
        @Singleton
        static Recommender recommender(TestBackend backend) {
            return spy(new TestRecommender(backend));
        }

        @dagger.Binds
        BotBackend b(TestBackend b);

        @dagger.Binds
        BeatmapsLoader l(TestBeatmapsLoader b);
    }
}
