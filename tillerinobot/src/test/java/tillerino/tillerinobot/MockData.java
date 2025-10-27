package tillerino.tillerinobot;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.inject.Singleton;
import lombok.Getter;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.data.ApiUser;
import tillerino.tillerinobot.diff.*;
import tillerino.tillerinobot.recommendations.BareRecommendation;
import tillerino.tillerinobot.recommendations.Recommender;
import tillerino.tillerinobot.recommendations.TopPlay;

/**
 * Backend implementation for the purposes of testing the Frontend.
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
public class MockData {
    @Getter
    static final Map<Integer, Integer> setIds = new HashMap<>();

    static {
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(MockData.class.getResourceAsStream("/beatmapIds.txt")))) {
            reader.lines().forEach(line -> {
                String[] s = line.split("\t");
                setIds.put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mockUser(
            String username,
            boolean isDonator,
            int rank,
            double pp,
            int id,
            BotBackend botBackend,
            OsuApi api,
            Recommender recommender)
            throws Exception {

        AtomicInteger lastVisited = new AtomicInteger();
        doAnswer(_ -> lastVisited.get()).when(botBackend).getLastVisitedVersion(username);
        doAnswer(inv -> {
                    lastVisited.set(inv.getArgument(1));
                    return null;
                })
                .when(botBackend)
                .setLastVisitedVersion(eq(username), anyInt());

        doReturn(isDonator ? 1 : 0).when(botBackend).getDonator(id);

        ApiUser apiUser = new ApiUser();
        apiUser.setUserId(id);
        apiUser.setUserName(username);
        apiUser.setRank(rank);
        apiUser.setPp(pp);
        apiUser.setCountry("XX");
        doReturn(apiUser).when(api).getUser(id, 0);
        doReturn(apiUser).when(api).getUser(username, 0);

        doAnswer(_ -> {
                    final double equivalent = pp / 20;
                    List<BeatmapMeta> maps = findBeatmaps(equivalent, 0, false);
                    List<TopPlay> plays = new ArrayList<>();
                    for (int i = 0; i < maps.size() && i < 50; i++) {
                        BeatmapMeta meta = maps.get(i);
                        plays.add(new TopPlay(
                                id, i, meta.getBeatmap().getBeatmapId(), meta.getMods(), meta.getPersonalPP()));
                    }
                    return plays;
                })
                .when(recommender)
                .loadTopPlays(id);
    }

    private static List<BeatmapMeta> findBeatmaps(final double equivalentPp, long requestMods, boolean nomod) {
        ArrayList<Long> mods = new ArrayList<>();
        if (requestMods == 0) {
            mods.add(0L);
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
                BeatmapMeta meta = createMockBeatmapMeta(i, m);
                if (Math.abs(1 - meta.getEstimates().getPP(.98) / equivalentPp) < .15) {
                    maps.add(meta);
                }
                meta.setPersonalPP((int) meta.getEstimates().getPP(.98));
            }
        }
        return maps;
    }

    @Nonnull
    public static OsuApiBeatmap createMockBeatmap(int beatmapid) {
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

    @Nonnull
    public static Collection<BareRecommendation> createMockRecommendations(
            List<TopPlay> topPlays, boolean nomod, long requestMods) {
        double equivalent = equivalentPp(topPlays);
        List<BeatmapMeta> maps = findBeatmaps(equivalent, requestMods, nomod);
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

    static double equivalentPp(List<TopPlay> plays) {
        plays = new ArrayList<>(plays);
        plays.sort(Comparator.comparingDouble(TopPlay::getPp).reversed());
        double ppSum = 0;
        double partialSum = 0;

        for (int i = 0; i < plays.size(); i++) {
            partialSum += Math.pow(.95, i);
            ppSum += plays.get(i).getPp() * Math.pow(.95, i);
        }

        return ppSum / partialSum;
    }

    public static BeatmapMeta createMockBeatmapMeta(int beatmapid, long mods) {
        OsuApiBeatmap beatmap = createMockBeatmap(beatmapid);

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
}
