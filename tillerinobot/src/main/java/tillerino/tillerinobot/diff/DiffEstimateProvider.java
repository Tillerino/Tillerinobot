package tillerino.tillerinobot.diff;

import static org.tillerino.osuApiModel.Mods.*;

import com.github.omkelderman.sandoku.DiffCalcResult;
import com.github.omkelderman.sandoku.DiffResult;
import com.github.omkelderman.sandoku.ProcessorApi;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.GameModes;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.DiffEstimate;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.rest.BeatmapResource;
import tillerino.tillerinobot.rest.BeatmapsService;

/** Bookkeeping around {@link DiffEstimate}. */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiffEstimateProvider {
    @BitwiseMods
    public static final long diffMods = getMask(TouchDevice, HalfTime, DoubleTime, Easy, HardRock, Flashlight);

    public final Semaphore calculatorSemaphore = new Semaphore(10, true);

    private final BeatmapsService beatmaps;

    private final OsuApi downloader;

    private final ProcessorApi calculator;

    private final DatabaseManager dbm;

    /**
     * Load or calculate diff estimates for multiple beatmaps.
     *
     * @param beatmaps best if these are unique
     * @return not all requested beatmaps might be in the result map. will not throw if a beatmap is not found.
     */
    public Map<BeatmapWithMods, BeatmapImpl> loadOrCalculateMultiple(
            Database database, Collection<BeatmapWithMods> beatmaps)
            throws SQLException, IOException, InterruptedException {
        Set<BeatmapWithMods> noMods =
                beatmaps.stream().map(BeatmapWithMods::nomod).collect(Collectors.toSet());
        Map<BeatmapWithMods, ApiBeatmap> apiBeatmaps = ApiBeatmap.loadOrDownload(database, noMods, 0, downloader);
        Map<BeatmapWithMods, DiffEstimate> diffEstimates = DiffEstimate.loadMultiple(
                database, beatmaps.stream().map(BeatmapWithMods::diffMods).collect(Collectors.toSet()));

        Map<BeatmapWithMods, BeatmapImpl> result = new LinkedHashMap<>();
        for (BeatmapWithMods beatmap : beatmaps) {
            MDC.put("beatmapid", String.valueOf(beatmap.beatmap()));
            ApiBeatmap cachedBeatmap = apiBeatmaps.get(beatmap.nomod());
            DiffEstimate estimate = diffEstimates.get(beatmap.diffMods());
            BeatmapImpl impl =
                    loadOrCalculatePreloaded(database, beatmap.beatmap(), beatmap.mods(), cachedBeatmap, estimate);
            if (impl != null) {
                result.put(beatmap, impl);
            }
        }
        return result;
    }

    @CheckForNull
    public BeatmapImpl loadOrCalculate(
            Database database, @BeatmapId int beatmapid, final @BitwiseMods long originalMods)
            throws SQLException, IOException, InterruptedException {
        BeatmapWithMods bwm = new BeatmapWithMods(beatmapid, originalMods);
        return loadOrCalculateMultiple(database, Collections.singleton(bwm)).get(bwm);
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    private BeatmapImpl loadOrCalculatePreloaded(
            Database database,
            @BeatmapId int beatmapid,
            @BitwiseMods long originalMods,
            ApiBeatmap cachedBeatmap,
            DiffEstimate estimate)
            throws SQLException, IOException, InterruptedException {
        if (cachedBeatmap != null && cachedBeatmap.getApproved() != OsuApiBeatmap.RANKED) {
            cachedBeatmap = ApiBeatmap.loadOrDownload(
                    database,
                    beatmapid,
                    0,
                    cachedBeatmap.getApproved() == OsuApiBeatmap.APPROVED ? 24 * 60 * 60 * 1000 : 10000,
                    downloader);
        }

        final long diffMods = getDiffMods(originalMods);
        if (cachedBeatmap == null) {
            // doesn't never existed or was deleted
            var _ = database.deleteFrom(DiffEstimate.class)
                    .execute("where beatmapid = ", beatmapid, " and mods = ", diffMods);
            return null;
        }

        if (estimate == null
                || estimate.getDataVersion() != SanDoku.VERSION
                || !Objects.equals(estimate.getMd5(), cachedBeatmap.getFileMd5())) {
            if (!calculatorSemaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                if (estimate != null && estimate.success) {
                    return DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(estimate);
                }
                calculatorSemaphore.acquire();
            }
            try {
                estimate = DiffEstimateProvider.calculateDiffEstimate(beatmapid, diffMods, beatmaps, calculator);
                if (estimate.failure != null) {
                    log.error(estimate.failure);
                }
            } finally {
                calculatorSemaphore.release();
            }

            try (Persister<DiffEstimate> persister = database.persister(DiffEstimate.class, Action.REPLACE)) {
                persister.persist(estimate);
            }
        }

        if (estimate.success) {
            return DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(estimate);
        }

        return null;
    }

    private static DiffEstimate calculateDiffEstimate(
            @BeatmapId int beatmapid, @BitwiseMods long mods, BeatmapsService beatmaps, ProcessorApi calculator) {

        BeatmapResource beatmap = beatmaps.byId(beatmapid);
        DiffEstimate estimate = new DiffEstimate(beatmapid, mods);
        estimate.md5 = beatmap.get().getFileMd5();

        String actualBeatmap;
        try {
            actualBeatmap = beatmap.getFile();
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Status.BAD_GATEWAY.getStatusCode()) {
                estimate.failure = "Beatmap file is corrupted. [https://github.com/ppy/osu-api/issues/130 reference]"
                        + e.getResponse().getEntity();
                log.warn("Beatmap download failed: {}", e.getResponse().getEntity(), e);
                return estimate;
            }
            log.error("Beatmap download failed: {}", e.getResponse().getEntity(), e);
            estimate.failure = "Beatmap download failed " + e.getResponse() + "; "
                    + e.getResponse().getEntity();
            return estimate;
        }

        DiffResult sanDoku;
        try {
            System.out.println("Requesting from SanDoku " + beatmapid + " " + mods);
            sanDoku =
                    calculator.processorCalcDiff(0, (int) mods, false, actualBeatmap.getBytes(StandardCharsets.UTF_8));
            DiffCalcResult r = sanDoku.getDiffCalcResult();
            if (r.getSliderCount() + r.getHitCircleCount() + r.getSpinnerCount() == 0) {
                estimate.failure = "Beatmap has no objects.";
                return estimate;
            }
        } catch (BadRequestException e) {
            estimate.failure = "SanDoku failed: "
                    + SanDoku.unwrapError(e).map(Object.class::cast).orElseGet(e::getResponse);
            return estimate;
        } catch (InternalServerErrorException | ClientErrorException e) {
            estimate.failure = "SanDoku failed: " + e.getResponse();
            return estimate;
        } catch (ProcessingException e) {
            log.error("San doku communication error", e);
            estimate.failure = "SanDoku communication error: " + e.getMessage();
            return estimate;
        }

        DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(sanDoku, estimate);
        estimate.success = true;

        return estimate;
    }

    /**
     * This is supposed to be called in a loop with a short delay for background maintenance. Once all beatmaps are
     * up-to-date, the function will idle for an hour.
     */
    public void updateDiffEstimatesAndWait() {
        try {
            if (updateDiffEstimates()) {
                log.debug("All diff estimates calculated with current version. Sleeping now.");
                Thread.sleep(3_600_000);
            }
        } catch (InterruptedException e) {
            // ok
            Thread.currentThread().interrupt();
        }
    }

    /** @return true if everything is up-to-date, false if should be called again in a little while. */
    boolean updateDiffEstimates() throws InterruptedException {
        try (Database db = dbm.getDatabase();
                Loader<DiffEstimate> loader = db.loader(DiffEstimate.class, "where `dataVersion` != ? limit 1")) {
            for (; ; ) {
                MDC.clear();
                Optional<DiffEstimate> outdated = loader.queryUnique(SanDoku.VERSION);
                if (!outdated.isPresent()) {
                    return true;
                }

                DiffEstimate diffEstimate = outdated.get();
                MDC.put("beatmapid", String.valueOf(diffEstimate.beatmapid));
                System.out.printf(
                        "Maintenance: Updating diff estimate %s/%s%n", diffEstimate.beatmapid, diffEstimate.mods);
                loadOrCalculate(db, diffEstimate.beatmapid, diffEstimate.mods);
                if (MdcUtils.getInt(MdcUtils.MDC_EXTERNAL_API_CALLS).orElse(0) == 0) {
                    // since we don't want to fire too many requests during maintenance, we break the loop here for a
                    // bit.
                    continue;
                }
                return false;
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            if (IRCBot.isTimeout(e)) {
                log.warn("Error while updating diff estimate", e);
            } else {
                log.error("Error while updating diff estimate", e);
            }
            return false;
        }
    }

    public PercentageEstimates getEstimates(@GameMode int gameMode, @BeatmapId int beatmapId, @BitwiseMods long mods)
            throws NoEstimatesException, SQLException, IOException, InterruptedException {
        if (gameMode != GameModes.OSU) {
            throw new NoEstimatesException();
        }

        try (var __ = PhaseTimer.timeTask("loadOrCalculateEstimates");
                Database database = dbm.getDatabase()) {
            // try to load with these exact mods
            BeatmapImpl diffEstimate = loadOrCalculate(database, beatmapId, mods);

            if (diffEstimate != null) {
                return new PercentageEstimatesImpl(diffEstimate, mods);
            }

            throw new NoEstimatesException();
        }
    }

    public BeatmapMeta loadBeatmap(final @BeatmapId int beatmapid, @BitwiseMods long mods, Language lang)
            throws SQLException, IOException, UserException, InterruptedException {
        ApiBeatmap beatmap;
        long diffMods = DiffEstimateProvider.getDiffMods(mods);
        try (Database database = dbm.getDatabase(); ) {
            beatmap = ApiBeatmap.loadOrDownload(database, beatmapid, diffMods, 7l * 24 * 60 * 60 * 1000, downloader);
        } catch (SQLException e) {
            throw new SQLException("exception loading beatmap " + beatmapid + " mods " + diffMods, e);
        }

        if (beatmap == null) return null;

        try {
            return new BeatmapMeta(beatmap, null, getEstimates(beatmap.getMode(), beatmap.getBeatmapId(), mods));
        } catch (NoEstimatesException e) {
            return null;
        }
    }

    /**
     * returns only TD, HT, DT, EZ, HR, and FL, converting NC to DT. Also includes HD, but only if FL is present
     *
     * @param mods
     * @return
     */
    @SuppressFBWarnings(value = "TQ", justification = "Producer")
    public static @BitwiseMods long getDiffMods(@BitwiseMods long mods) {
        if (Nightcore.is(mods)) {
            mods |= getMask(DoubleTime);
            mods ^= getMask(Nightcore);
        }

        boolean hdfl = Mods.Flashlight.is(mods) && Hidden.is(mods);

        mods = mods & diffMods;

        if (hdfl) {
            // re-apply HD if used in combination with FL
            mods |= getMask(Hidden);
        }

        return mods;
    }

    public static class NoEstimatesException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
