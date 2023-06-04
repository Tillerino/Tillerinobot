package tillerino.tillerinobot.diff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.function.FailableRunnable;
import org.awaitility.Awaitility;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.InjectionRunner;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import org.tillerino.ppaddict.util.TestModule;

import tillerino.tillerinobot.AbstractDatabaseTest.DockeredMysqlModule;
import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiBeatmapTest;
import tillerino.tillerinobot.data.DiffEstimate;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.diff.sandoku.SanDokuResponse;
import tillerino.tillerinobot.diff.sandoku.SanDokuResponse.SanDokuDiffCalcResult;
import tillerino.tillerinobot.rest.BeatmapsServiceImpl;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@RunWith(InjectionRunner.class)
@TestModule(value = { DockeredMysqlModule.class, BeatmapsServiceImpl.Module.class },
		mocks = { SanDoku.class, Downloader.class, BeatmapDownloader.class })
public class DiffEstimateProviderTest {
	@Inject
	SanDoku sanDoku;
	@Inject
	Downloader downloader;
	@Inject
	BeatmapDownloader beatmapDownloader;
	@Inject
	DiffEstimateProvider provider;
	@Inject
	DatabaseManager databaseManager;
	@ClassRule
	public final static ExecutorServiceRule exec = new ExecutorServiceRule(Executors::newSingleThreadExecutor);

	@Rule
	public final LogRule logRule = TestAppender.rule();

	@Rule
	public MysqlDatabaseLifecycle lifecycle = new MysqlDatabaseLifecycle();

	@Test
	public void cached() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			String beatmapContent = "bla";
			when(beatmapDownloader.getActualBeatmap(123)).thenReturn(beatmapContent);

			ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
			beatmap.setBeatmapId(123);
			beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

			mockSanDokuResponse(beatmapContent, 1.919);

			when(downloader.getBeatmap(123, 0L, ApiBeatmap.class)).thenReturn(beatmap);
			assertThat(provider.loadOrCalculate(database, 123, 0))
				.isNotNull()
				.satisfies(impl -> assertThat(impl)
						.hasFieldOrPropertyWithValue("aim", 1.919f));
			assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

			// MD5 not changed, so only one invocation
			verify(sanDoku, only()).getDiff(0, 0, beatmapContent.getBytes());
		}

		// check that background maintenance wouldn't update this
		Thread thread = new Thread(() -> provider.updateDiffEstimatesAndWait());
		thread.start();
		Awaitility.await()
			.pollInterval(10, TimeUnit.MILLISECONDS)
			.untilAsserted(() -> logRule.assertThat().anyMatch(event -> event.getMessage().getFormattedMessage().contains("Sleeping now.")));
		thread.interrupt();
		thread.join(1000);
		assertThat(thread.isAlive()).isFalse();
	}

	@Test
	public void md5Changed() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			String beatmapContent = "bla";
			when(beatmapDownloader.getActualBeatmap(123)).thenReturn(beatmapContent);

			ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
			beatmap.setBeatmapId(123);
			beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

			DiffEstimate oldDiffEstimate = new DiffEstimate(123, 0);
			oldDiffEstimate.setSuccess(true);
			oldDiffEstimate.setMd5("old md5");
			database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

			mockSanDokuResponse(beatmapContent, 1.919);

			when(downloader.getBeatmap(123, 0L, ApiBeatmap.class)).thenReturn(beatmap);
			assertThat(provider.loadOrCalculate(database, 123, 0))
				.isNotNull()
				.satisfies(impl -> assertThat(impl)
						.hasFieldOrPropertyWithValue("aim", 1.919f));
			assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

			// MD5 not changed, so only one invocation
			verify(sanDoku, only()).getDiff(0, 0, beatmapContent.getBytes());
		}
	}

	@Test
	public void versionChanged() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			setUpOutdatedVersionDiffEstimate(database, "bla", 123);
			assertThat(provider.loadOrCalculate(database, 123, 0))
				.isNotNull()
				.satisfies(impl -> assertThat(impl)
						.hasFieldOrPropertyWithValue("aim", 1.919f));
			assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

			// MD5 not changed, so only one invocation
			verify(sanDoku, only()).getDiff(0, 0, "bla".getBytes());
		}
	}

	private void setUpOutdatedVersionDiffEstimate(Database database, String beatmapContent, int beatmapId) throws SQLException, IOException {
		ActualBeatmap actualBeatmap = new ActualBeatmap();
		actualBeatmap.setBeatmapid(beatmapId);
		actualBeatmap.setContent(beatmapContent.getBytes());
		actualBeatmap.setDownloaded(System.currentTimeMillis());
		actualBeatmap.setHash(DigestUtils.md5Hex(beatmapContent));
		databaseManager.persist(actualBeatmap, Action.INSERT);

		ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
		beatmap.setBeatmapId(beatmapId);
		beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));
		database.persist(beatmap, Action.INSERT);

		DiffEstimate oldDiffEstimate = new DiffEstimate(beatmapId, 0);
		oldDiffEstimate.setSuccess(true);
		oldDiffEstimate.setMd5(DigestUtils.md5Hex(beatmapContent));
		oldDiffEstimate.setDataVersion(255);
		database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

		mockSanDokuResponse(beatmapContent, 1.919);

	}

	@Test
	public void deleteOld() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			DiffEstimate oldDiffEstimate = new DiffEstimate(123, 0);
			oldDiffEstimate.setSuccess(true);
			oldDiffEstimate.setMd5("no md5");
			database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

			assertThat(provider.loadOrCalculate(database, 123, 0)).isNull();
			assertThat(database.loader(DiffEstimate.class, "").query()).isEmpty();
		}
	}

	@Test
	public void oneBackgroundMaintenance() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			setUpOutdatedVersionDiffEstimate(database, "bla", 123);

			// fake an outdated betmap in the database
			ActualBeatmap actualBeatmap = new ActualBeatmap();
			actualBeatmap.setBeatmapid(123);
			actualBeatmap.setContent("bla old".getBytes());
			actualBeatmap.setDownloaded(0); // so it can be updated
			actualBeatmap.setHash(DigestUtils.md5Hex("bla old"));
			databaseManager.persist(actualBeatmap, Action.REPLACE);
			when(beatmapDownloader.getActualBeatmap(123)).thenReturn("bla");

			assertThat(database.loadUnique(DiffEstimate.class, 123, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));

			runAsyncAndWait(provider::updateDiffEstimatesAndWait); // since we downloaded, this won't sleep because it hasn't exhausted all beatmaps.
			verify(beatmapDownloader).getActualBeatmap(123);
			assertThat(database.loadUnique(DiffEstimate.class, 123, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", (double) 1.919f));
		}
	}

	@Test
	public void noApiUpdatesArePerformedInBatch() throws Exception {
		try (Database database = databaseManager.getDatabase()) {
			setUpOutdatedVersionDiffEstimate(database, "bla123", 123);
			setUpOutdatedVersionDiffEstimate(database, "bla456", 456);

			assertThat(database.loadUnique(DiffEstimate.class, 123, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));
			assertThat(database.loadUnique(DiffEstimate.class, 456, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));

			runAsyncAndWait(provider::updateDiffEstimates);

			assertThat(database.loadUnique(DiffEstimate.class, 123, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", (double) 1.919f));
			assertThat(database.loadUnique(DiffEstimate.class, 456, 0L)).hasValueSatisfying(
					diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", (double) 1.919f));
		}
	}

	private void mockSanDokuResponse(String beatmapContent, double aim) {
		SanDokuResponse response = SanDokuResponse.builder()
				.diffCalcResult(SanDokuDiffCalcResult.builder()
						.aim(aim)
						.hitCircleCount(1)
						.build())
				.build();
		when(sanDoku.getDiff(0, 0, beatmapContent.getBytes())).thenReturn(response);
	}

	private static <E extends Exception> void runAsyncAndWait(FailableRunnable<E> r) throws Exception {
		exec.submit(() -> { r.run(); return null; }).get(1, TimeUnit.SECONDS);
	}
}
