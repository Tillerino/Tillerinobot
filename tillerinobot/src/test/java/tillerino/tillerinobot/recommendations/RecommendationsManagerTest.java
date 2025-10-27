package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import tillerino.tillerinobot.*;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.lang.Default;

public class RecommendationsManagerTest extends TestBase {

    OsuApiUser user;

    @BeforeEach
    public void createUser() throws Exception {
        TestBase.mockBeatmapMetas(diffEstimateProvider);
        MockData.mockUser("donator", true, 1, 1000, 123, backend, osuApi, standardRecommender);

        user = pullThrough.downloadUser("donator");
    }

    @Test
    public void testAutoIncrement() throws SQLException {
        GivenRecommendation rec = new GivenRecommendation(323456789, 2, 3, 4);

        dbm.persist(rec, Action.INSERT);

        try (Loader<GivenRecommendation> loader = db.loader(GivenRecommendation.class, "")) {
            GivenRecommendation givenRecommendation = loader.queryUnique().get();
            assertThat(givenRecommendation.getId()).isPositive().isNotEqualTo(323456789);
        }
    }

    @Test
    public void testPredicateParser() throws Exception {
        RecommendationRequest samplerSettings =
                recommendationsManager.parseSamplerSettings(user, "gamma AR=9", new Default());

        assertEquals(1, samplerSettings.predicates().size());

        // Test "nc" alias for nightcore
        // Gives double-time recommendations
        samplerSettings = recommendationsManager.parseSamplerSettings(user, "nc", new Default());

        assertTrue(Mods.DoubleTime.is(samplerSettings.requestedMods()));
    }

    @Test
    public void testContinuousMods() throws Exception {
        RecommendationRequest samplerSettings =
                recommendationsManager.parseSamplerSettings(user, "hdhr", new Default());

        assertEquals(Mods.getMask(Mods.Hidden, Mods.HardRock), samplerSettings.requestedMods());

        samplerSettings = recommendationsManager.parseSamplerSettings(user, "hdhr dt", new Default());

        assertEquals(Mods.getMask(Mods.Hidden, Mods.HardRock, Mods.DoubleTime), samplerSettings.requestedMods());
    }

    @Test
    public void testContradiction() {
        assertThatThrownBy(() -> recommendationsManager.parseSamplerSettings(user, "ar=1 ar=2", new Default()))
                .isInstanceOf(UserException.class);
    }

    @Test
    public void testSaveRecommendations() throws Exception {
        recommendationsManager.saveGivenRecommendation(1015, 16, 64);

        List<GivenRecommendation> saved = recommendationsManager.loadGivenRecommendations(1015);

        assertEquals(1, saved.size());

        GivenRecommendation rec = saved.getFirst();

        assertEquals(1015, rec.getUserid());
        assertEquals(16, rec.getBeatmapid());
        assertEquals(64, rec.getMods());
    }

    @Test
    public void forgettingRecommendations() throws Exception {
        recommendationsManager.saveGivenRecommendation(465, 16, 64);
        recommendationsManager.saveGivenRecommendation(863, 42, 5634);

        assertEquals(1, recommendationsManager.loadGivenRecommendations(465).size());
        assertEquals(1, recommendationsManager.loadGivenRecommendations(863).size());

        recommendationsManager.forgetRecommendations(465);

        assertEquals(0, recommendationsManager.loadGivenRecommendations(465).size());
        assertEquals(1, recommendationsManager.loadGivenRecommendations(863).size());
    }

    @Test
    public void testHiding() throws Exception {
        // save a recommendation and reload it
        recommendationsManager.saveGivenRecommendation(1954, 2, 0);
        List<GivenRecommendation> recs = recommendationsManager.loadVisibleRecommendations(1954);
        assertEquals(2, recs.getFirst().getBeatmapid());

        // remember its date
        long firstRecDate = recs.getFirst().getDate();
        Thread.sleep(30);

        // save another recommendation and hide it
        recommendationsManager.saveGivenRecommendation(1954, 3, 0);
        recs = recommendationsManager.loadVisibleRecommendations(1954);
        assertEquals(2, recs.size());
        assertTrue(recs.get(0).getDate() > recs.get(1).getDate());
        recommendationsManager.hideRecommendation(1954, 3, 0);

        // load visible recommendations and check if this is the earlier one
        recs = recommendationsManager.loadVisibleRecommendations(1954);
        assertEquals(firstRecDate, recs.getFirst().getDate());

        // hide again and check if list is empty now
        recommendationsManager.hideRecommendation(1954, 2, 0);
        recs = recommendationsManager.loadVisibleRecommendations(1954);
        assertEquals(0, recs.size());
    }

    @Test
    public void defaultSettings() throws Exception {
        TestBase.mockRecommendations(recommender);
        assertThat(recommendationsManager.getRecommendation(user, "", new Default()))
                .isNotNull();
        verify(recommender).loadRecommendations(any(), any(), eq(Model.GAMMA10), eq(false), eq(0L));
    }

    @Test
    public void testGamma10() throws Exception {
        TestBase.mockRecommendations(recommender);
        assertThat(recommendationsManager.getRecommendation(user, "gamma10", new Default()))
                .isNotNull();
        verify(recommender).loadRecommendations(any(), any(), eq(Model.GAMMA10), anyBoolean(), anyLong());
    }

    @Test
    public void gamma7NotRestricted() throws Exception {
        MockData.mockUser("guy", false, 123, 123, 123, backend, osuApi, standardRecommender);
        TestBase.mockRecommendations(recommender);
        user = pullThrough.downloadUser("guy");
        assertThat(recommendationsManager.getRecommendation(user, "gamma7", new Default()))
                .isNotNull();
    }

    @Test
    public void succ() throws Exception {
        runShift("succ", 25);
    }

    @Test
    public void succer() throws Exception {
        runShift("succer", 12);
    }

    @Test
    public void succerberg() throws Exception {
        runShift("succerberg", 5);
    }

    private void runShift(String mode, int limit) throws Exception {
        MockData.mockUser("guy", true, 123, 1000, 123, backend, osuApi, standardRecommender);
        TestBase.mockRecommendations(standardRecommender);

        user = pullThrough.downloadUser("guy");

        List<TopPlay> topPlays = new ArrayList<>(recommender.loadTopPlays(user.getUserId()));
        assertThat(topPlays).hasSize(50);
        topPlays.sort(Comparator.comparingDouble(TopPlay::getPp));

        assertThat(recommendationsManager.getRecommendation(user, mode, new Default()))
                .isNotNull();
        verify(recommender)
                .loadRecommendations(
                        argThat(l -> l.equals(topPlays.subList(0, limit))),
                        any(),
                        eq(Model.GAMMA10),
                        anyBoolean(),
                        anyLong());
    }
}
