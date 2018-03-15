package tillerino.tillerinobot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.recommendations.RecommendationRequest;

public class RecommendationsManagerTest extends AbstractDatabaseTest {
	TestBackend backend = new TestBackend(false);

	RecommendationsManager manager;

	OsuApiUser user;

	@Before
	public void createUser() throws SQLException, IOException {
		backend.hintUser("donator", true, 1, 1000);

		user = backend.downloadUser("donator");
	}
	
	@Before
	public void createRecommendationsManager() {
		manager = new RecommendationsManager(backend, recommendationsRepo, em);
	}

	@Test
	public void testPredicateParser() throws Exception {
		RecommendationRequest samplerSettings = manager.parseSamplerSettings(user, "gamma AR=9", new Default());

		assertEquals(1, samplerSettings.getPredicates().size());

		// Test "nc" alias for nightcore
		// Gives double-time recommendations
		samplerSettings = manager.parseSamplerSettings(user, "nc", new Default());

		assertTrue(Mods.DoubleTime.is(samplerSettings.getRequestedMods()));
	}

	@Test
	public void testContinuousMods() throws Exception {
		RecommendationRequest samplerSettings = manager.parseSamplerSettings(user, "hdhr", new Default());

		assertEquals(Mods.getMask(Mods.Hidden, Mods.HardRock), samplerSettings.getRequestedMods());

		samplerSettings = manager.parseSamplerSettings(user, "hdhr dt", new Default());

		assertEquals(Mods.getMask(Mods.Hidden, Mods.HardRock, Mods.DoubleTime), samplerSettings.getRequestedMods());
	}

	@Test(expected = UserException.class)
	public void testContradiction() throws Exception {
		manager.parseSamplerSettings(user, "ar=1 ar=2", new Default());
	}
	
	@Test
	public void testSaveRecommendations() throws Exception {
		manager.saveGivenRecommendation(15, 16, 64);
		
		List<GivenRecommendation> saved = manager.loadGivenRecommendations(15);
		
		assertEquals(1, saved.size());
		
		GivenRecommendation rec = saved.get(0);

		assertEquals(15, rec.getUserid());
		assertEquals(16, rec.getBeatmapid());
		assertEquals(64, rec.getMods());
	}
	
	@Test
	public void forgettingRecommendations() throws Exception {
		manager.saveGivenRecommendation(465, 16, 64);
		manager.saveGivenRecommendation(863, 42, 5634);

		assertEquals(1, manager.loadGivenRecommendations(465).size());
		assertEquals(1, manager.loadGivenRecommendations(863).size());
		
		manager.forgetRecommendations(465);

		assertEquals(0, manager.loadGivenRecommendations(465).size());
		assertEquals(1, manager.loadGivenRecommendations(863).size());
	}

	@Test
	public void testHiding() throws Exception {
		RecommendationsManager recMan = new RecommendationsManager(null, recommendationsRepo, em);

		// save a recommendation and reload it
		recMan.saveGivenRecommendation(1954, 2, 0);
		List<GivenRecommendation> recs = recMan.loadVisibleRecommendations(1954);
		assertEquals(2, recs.get(0).getBeatmapid());
		
		// remember its date
		long firstRecDate = recs.get(0).getDate();
		Thread.sleep(30);

		// save another recommendation and hide it
		recMan.saveGivenRecommendation(1954, 3, 0);
		recs = recMan.loadVisibleRecommendations(1954);
		assertEquals(2, recs.size());
		assertTrue(recs.get(0).getDate() > recs.get(1).getDate());
		recMan.hideRecommendation(1954, 3, 0);
		
		// load visible recommendations and check if this is the earlier one
		recs = recMan.loadVisibleRecommendations(1954);
		assertEquals(firstRecDate, recs.get(0).getDate());
		
		// hide again and check if list is empty now
		recMan.hideRecommendation(1954, 2, 0);
		recs = recMan.loadVisibleRecommendations(1954);
		assertEquals(0, recs.size());
	}
}
