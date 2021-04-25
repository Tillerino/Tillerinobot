package tillerino.tillerinobot.recommendations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.lang.Default;

@TestModule(TestBackend.Module.class)
public class RecommendationsManagerTest extends AbstractDatabaseTest {
	@Inject
	TestBackend backend;

	@Inject
	RecommendationsManager manager;

	OsuApiUser user;

	@Before
	public void createUser() throws SQLException, IOException {
		backend.hintUser("donator", true, 1, 1000);

		user = backend.downloadUser("donator");
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
		manager.saveGivenRecommendation(1015, 16, 64);

		List<GivenRecommendation> saved = manager.loadGivenRecommendations(1015);

		assertEquals(1, saved.size());

		GivenRecommendation rec = saved.get(0);

		assertEquals(1015, rec.getUserid());
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
		// save a recommendation and reload it
		manager.saveGivenRecommendation(1954, 2, 0);
		List<GivenRecommendation> recs = manager.loadVisibleRecommendations(1954);
		assertEquals(2, recs.get(0).getBeatmapid());

		// remember its date
		long firstRecDate = recs.get(0).getDate();
		Thread.sleep(30);

		// save another recommendation and hide it
		manager.saveGivenRecommendation(1954, 3, 0);
		recs = manager.loadVisibleRecommendations(1954);
		assertEquals(2, recs.size());
		assertTrue(recs.get(0).getDate() > recs.get(1).getDate());
		manager.hideRecommendation(1954, 3, 0);

		// load visible recommendations and check if this is the earlier one
		recs = manager.loadVisibleRecommendations(1954);
		assertEquals(firstRecDate, recs.get(0).getDate());

		// hide again and check if list is empty now
		manager.hideRecommendation(1954, 2, 0);
		recs = manager.loadVisibleRecommendations(1954);
		assertEquals(0, recs.size());
	}

	@Test
	public void defaultSettings() throws Exception {
		assertThat(manager.getRecommendation(user, "", new Default())).isNotNull();
		verify(backend).loadRecommendations(user.getUserId(), Collections.emptyList(), Model.GAMMA5, false, 0L);
	}

	@Test
	public void testGamma5() throws Exception {
		assertThat(manager.getRecommendation(user, "gamma5", new Default())).isNotNull();
		verify(backend).loadRecommendations(anyInt(), any(), eq(Model.GAMMA5), anyBoolean(), anyLong());
	}

	@Test
	public void gamma5NotRestricted() throws Exception {
		backend.hintUser("guy", false, 123, 123);
		user = backend.downloadUser("guy");
		assertThat(manager.getRecommendation(user, "gamma5", new Default())).isNotNull();
	}
}
