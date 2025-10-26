package tillerino.tillerinobot.diff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.omkelderman.sandoku.DiffCalcResult;
import com.github.omkelderman.sandoku.DiffResult;
import com.github.omkelderman.sandoku.ProcessorApi;
import dagger.Component;
import dagger.Module;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import tillerino.tillerinobot.AbstractDatabaseTest;
import tillerino.tillerinobot.MysqlContainer.MysqlDatabaseLifecycle;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiBeatmapTest;
import tillerino.tillerinobot.data.DiffEstimate;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;
import tillerino.tillerinobot.rest.BeatmapsServiceImpl;

public class DiffEstimateProviderTest extends AbstractDatabaseTest {
    @Singleton
    @Component(modules = {DockeredMysqlModule.class, BeatmapsServiceImpl.Module.class, Mocks.class})
    interface Injector {
        void inject(DiffEstimateProviderTest t);
    }

    @Module
    interface Mocks {

        @dagger.Provides
        @Singleton
        static ProcessorApi sanDoku() {
            return mock(ProcessorApi.class);
        }

        @dagger.Provides
        @Singleton
        static OsuApi osuApi() {
            return mock(OsuApi.class);
        }

        @dagger.Provides
        @Singleton
        static BeatmapDownloader beatmapDownloader() {
            return mock(BeatmapDownloader.class);
        }
    }

    {
        DaggerDiffEstimateProviderTest_Injector.create().inject(this);
    }

    @Inject
    ProcessorApi sanDoku;

    @Inject
    OsuApi downloader;

    @Inject
    BeatmapDownloader beatmapDownloader;

    @Inject
    DiffEstimateProvider provider;

    @RegisterExtension
    public final ExecutorServiceRule exec = new ExecutorServiceRule(Executors::newSingleThreadExecutor);

    @RegisterExtension
    public final LogRule logRule = TestAppender.rule(DiffEstimateProvider.class);

    @RegisterExtension
    public MysqlDatabaseLifecycle lifecycle = new MysqlDatabaseLifecycle();

    @Test
    public void cached() throws Exception {
        try (Database database = dbm.getDatabase()) {
            String beatmapContent = "bla";
            when(beatmapDownloader.getActualBeatmap(123)).thenReturn(beatmapContent);

            ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
            beatmap.setBeatmapId(123);
            beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

            mockSanDokuResponse(beatmapContent, 1.919);

            when(downloader.getBeatmap(123, 0L)).thenReturn(beatmap);
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull().satisfies(impl -> assertThat(impl)
                    .hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

            // MD5 not changed, so only one invocation
            verify(sanDoku, only()).processorCalcDiff(0, 0, false, beatmapContent.getBytes());
        }

        // check that background maintenance wouldn't update this
        Thread thread = new Thread(() -> provider.updateDiffEstimatesAndWait());
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
            when(beatmapDownloader.getActualBeatmap(123)).thenReturn(beatmapContent);

            ApiBeatmap beatmap = ApiBeatmapTest.newApiBeatmap();
            beatmap.setBeatmapId(123);
            beatmap.setFileMd5(DigestUtils.md5Hex(beatmapContent));

            DiffEstimate oldDiffEstimate = new DiffEstimate(123, 0);
            oldDiffEstimate.setSuccess(true);
            oldDiffEstimate.setMd5("old md5");
            database.persister(DiffEstimate.class, Action.INSERT).persist(oldDiffEstimate);

            mockSanDokuResponse(beatmapContent, 1.919);

            when(downloader.getBeatmap(123, 0L)).thenReturn(beatmap);
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull().satisfies(impl -> assertThat(impl)
                    .hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

            // MD5 not changed, so only one invocation
            verify(sanDoku, only()).processorCalcDiff(0, 0, false, beatmapContent.getBytes());
        }
    }

    @Test
    public void versionChanged() throws Exception {
        try (Database database = dbm.getDatabase()) {
            setUpOutdatedVersionDiffEstimate(database, "bla", 123);
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull().satisfies(impl -> assertThat(impl)
                    .hasFieldOrPropertyWithValue("AimDifficulty", 1.919));
            assertThat(provider.loadOrCalculate(database, 123, 0)).isNotNull();

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

            assertThat(provider.loadOrCalculate(database, 123, 0)).isNull();
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
            when(beatmapDownloader.getActualBeatmap(123)).thenReturn("bla");

            assertThat(database.selectUnique(DiffEstimate.class).execute("where beatmapid = ", 123, " and mods = ", 0L))
                    .hasValueSatisfying(
                            diffEstimate -> assertThat(diffEstimate).hasFieldOrPropertyWithValue("aim", 0.0));

            runAsyncAndWait(
                    provider::updateDiffEstimatesAndWait); // since we downloaded, this won't sleep because it hasn't
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

            runAsyncAndWait(provider::updateDiffEstimates);

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
        when(sanDoku.processorCalcDiff(0, 0, false, beatmapContent.getBytes())).thenReturn(response);
    }

    private <E extends Exception> void runAsyncAndWait(FailableRunnable<E> r) throws Exception {
        exec.submit(() -> {
                    r.run();
                    return null;
                })
                .get(1, TimeUnit.SECONDS);
    }
}
