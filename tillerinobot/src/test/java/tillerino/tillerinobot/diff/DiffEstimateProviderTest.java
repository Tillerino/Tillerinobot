package tillerino.tillerinobot.diff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import com.github.omkelderman.sandoku.DiffCalcResult;
import com.github.omkelderman.sandoku.DiffResult;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.function.FailableRunnable;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.ppaddict.util.ExecutorServiceRule;
import org.tillerino.ppaddict.util.TestAppender;
import org.tillerino.ppaddict.util.TestAppender.LogRule;
import tillerino.tillerinobot.TestBase;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiBeatmapTest;
import tillerino.tillerinobot.data.DiffEstimate;

public class DiffEstimateProviderTest extends TestBase {
    @RegisterExtension
    public final ExecutorServiceRule exec = new ExecutorServiceRule(Executors::newSingleThreadExecutor);

    @RegisterExtension
    public final LogRule logRule = TestAppender.rule(DiffEstimateProvider.class);

    @Test
    public void cached() throws Exception {
        try (Database database = dbm.getDatabase()) {
            String beatmapContent = "bla";
            doReturn(beatmapContent).when(beatmapDownloader).getActualBeatmap(123);

            ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
            beatmap.setBeatmapId(123);
            beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

            mockSanDokuResponse(beatmapContent, 1.919);

            doReturn(beatmap).when(osuApi).getBeatmap(123, 0L);
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0))
                    .isNotNull()
                    .satisfies(impl -> assertThat(impl).hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0)).isNotNull();

            // MD5 not changed, so only one invocation
            verify(sanDoku, only()).processorCalcDiff(0, 0, false, beatmapContent.getBytes());
        }

        // check that background maintenance wouldn't update this
        Thread thread = new Thread(() -> diffEstimateProvider.updateDiffEstimatesAndWait());
        thread.start();
        Awaitility.await().pollInterval(10, TimeUnit.MILLISECONDS).untilAsserted(() -> logRule.assertThat()
                .anyMatch(event -> event.getMessage().contains("Sleeping now.")));
        thread.interrupt();
        thread.join(1000);
        assertThat(thread.isAlive()).isFalse();
    }

    @Test
    public void md5Changed() throws Exception {
        try (Database database = dbm.getDatabase()) {
            String beatmapContent = "bla";
            doReturn(beatmapContent).when(beatmapDownloader).getActualBeatmap(123);

            ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
            beatmap.setBeatmapId(123);
            beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

            DiffEstimate oldDiffEstimate = new DiffEstimate(123, 0);
            oldDiffEstimate.setSuccess(true);
            oldDiffEstimate.setMd5("old md5");
            database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

            mockSanDokuResponse(beatmapContent, 1.919);

            doReturn(beatmap).when(osuApi).getBeatmap(123, 0L);
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0))
                    .isNotNull()
                    .satisfies(impl -> assertThat(impl).hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0)).isNotNull();

            // MD5 not changed, so only one invocation
            verify(sanDoku, only()).processorCalcDiff(0, 0, false, beatmapContent.getBytes());
        }
    }

    @Test
    public void versionChanged() throws Exception {
        try (Database database = dbm.getDatabase()) {
            setUpOutdatedVersionDiffEstimate(database, "bla", 123);
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0))
                    .isNotNull()
                    .satisfies(impl -> assertThat(impl).hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0)).isNotNull();

            // MD5 not changed, so only one invocation
            verify(sanDoku, only()).processorCalcDiff(0, 0, false, "bla".getBytes());
        }
    }

    private void setUpOutdatedVersionDiffEstimate(Database database, String beatmapContent, int beatmapId)
            throws SQLException {
        ActualBeatmap actualBeatmap = new ActualBeatmap();
        actualBeatmap.setBeatmapid(beatmapId);
        actualBeatmap.setContent(beatmapContent.getBytes());
        actualBeatmap.setDownloaded(System.currentTimeMillis());
        actualBeatmap.setHash(DigestUtils.md5Hex(beatmapContent));
        dbm.persist(actualBeatmap, Action.INSERT);

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
        try (Database database = dbm.getDatabase()) {
            DiffEstimate oldDiffEstimate = new DiffEstimate(123, 0);
            oldDiffEstimate.setSuccess(true);
            oldDiffEstimate.setMd5("no md5");
            database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

            assertThat(diffEstimateProvider.loadOrCalculate(database, 123, 0)).isNull();
            assertThat(database.loader(DiffEstimate.class, "").query()).isEmpty();
        }
    }

    @Test
    public void oneBackgroundMaintenance() throws Exception {
        try (Database database = dbm.getDatabase()) {
            setUpOutdatedVersionDiffEstimate(database, "bla", 123);

            // fake an outdated betmap in the database
            ActualBeatmap actualBeatmap = new ActualBeatmap();
            actualBeatmap.setBeatmapid(123);
            actualBeatmap.setContent("bla old".getBytes());
            actualBeatmap.setDownloaded(0); // so it can be updated
            actualBeatmap.setHash(DigestUtils.md5Hex("bla old"));
            dbm.persist(actualBeatmap, Action.REPLACE);
            doReturn("bla").when(beatmapDownloader).getActualBeatmap(123);

            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 123, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));

            runAsyncAndWait(
                    diffEstimateProvider
                            ::updateDiffEstimatesAndWait); // since we downloaded, this won't sleep because it hasn't
            // exhausted all beatmaps.
            verify(beatmapDownloader).getActualBeatmap(123);
            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 123, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 1.919));
        }
    }

    @Test
    public void noApiUpdatesArePerformedInBatch() throws Exception {
        try (Database database = dbm.getDatabase()) {
            setUpOutdatedVersionDiffEstimate(database, "bla123", 123);
            setUpOutdatedVersionDiffEstimate(database, "bla456", 456);

            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 123, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));
            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 456, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));

            runAsyncAndWait(diffEstimateProvider::updateDiffEstimates);

            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 123, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 1.919));
            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 456, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 1.919));
        }
    }

    private void mockSanDokuResponse(String beatmapContent, double aim) {
        DiffResult response = new DiffResult()
                .diffCalcResult(new DiffCalcResult()
                        .aimDifficulty(aim)
                        .hitCircleCount(1)
                        .sliderCount(2)
                        .spinnerCount(3));
        doReturn(response).when(sanDoku).processorCalcDiff(0, 0, false, beatmapContent.getBytes());
    }

    private <E extends Exception> void runAsyncAndWait(FailableRunnable<E> r) throws Exception {
        exec.submit(() -> {
                    r.run();
                    return null;
                })
                .get(1, TimeUnit.SECONDS);
    }
}
