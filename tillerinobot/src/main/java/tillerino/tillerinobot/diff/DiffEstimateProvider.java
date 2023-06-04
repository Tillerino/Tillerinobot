package tillerino.tillerinobot.diff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.slf4j.MDC;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.DiffEstimate;
import tillerino.tillerinobot.diff.sandoku.SanDoku;
import tillerino.tillerinobot.diff.sandoku.SanDokuResponse;
import tillerino.tillerinobot.diff.sandoku.SanDokuResponse.SanDokuDiffCalcResult;
import tillerino.tillerinobot.rest.BeatmapResource;
import tillerino.tillerinobot.rest.BeatmapsService;

/**
 * Bookkeeping around {@link DiffEstimate}.
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiffEstimateProvider {
	public final Semaphore calculatorSemaphore = new Semaphore(10, true);

	private final BeatmapsService beatmaps;

	private final Downloader downloader;

	private final SanDoku calculator;

	private final DatabaseManager dbm;

	/**
	 * @param database 
	 * @param beatmapid 
	 * @param originalMods 
	 */
	@CheckForNull
	public BeatmapImpl loadOrCalculate(Database database, @BeatmapId int beatmapid, final @BitwiseMods long originalMods) throws SQLException, IOException, InterruptedException {
		MDC.put("beatmapid", String.valueOf(beatmapid));

		ApiBeatmap cachedBeatmap = ApiBeatmap.loadOrDownload(database, beatmapid, 0, 0, downloader);

		if (cachedBeatmap != null && cachedBeatmap.getApproved() != OsuApiBeatmap.RANKED) {
			cachedBeatmap = ApiBeatmap.loadOrDownload(database, beatmapid, 0, cachedBeatmap.getApproved() == OsuApiBeatmap.APPROVED ? 24 * 60 * 60 * 1000 : 10000, downloader);
		}

		final long diffMods = Beatmap.getDiffMods(originalMods);
		if (cachedBeatmap == null) {
			// doesn't never existed or was deleted
			database.delete(DiffEstimate.class, false, beatmapid, diffMods);
			return null;
		}

		DiffEstimate estimate = database.loadUnique(DiffEstimate.class, beatmapid, diffMods).orElse(null);

		if(estimate == null || estimate.getDataVersion() != SanDoku.VERSION || !Objects.equals(estimate.getMd5(), cachedBeatmap.getFileMd5())) {
			if (!calculatorSemaphore.tryAcquire(1, TimeUnit.SECONDS)) {
				if (estimate != null && estimate.success) {
					return DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(estimate);
				}
				calculatorSemaphore.acquire();
			}
			try {
				estimate = DiffEstimateProvider.calculateDiffEstimate(beatmapid, diffMods, beatmaps, calculator);
			} finally {
				calculatorSemaphore.release();
			}

			try(Persister<DiffEstimate> persister = database.persister(DiffEstimate.class, Action.REPLACE)) {
				persister.persist(estimate);
			}
		}

		if (estimate.success) {
			return DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(estimate);
		}

		return null;
	}

	private static DiffEstimate calculateDiffEstimate(@BeatmapId int beatmapid, @BitwiseMods long mods,
			BeatmapsService beatmaps, SanDoku calculator) {
	
		BeatmapResource beatmap = beatmaps.byId(beatmapid);
		DiffEstimate estimate = new DiffEstimate(beatmapid, mods);
		estimate.md5 = beatmap.get().getFileMd5();

		String actualBeatmap;
		try {
			actualBeatmap = beatmap.getFile();
		} catch (WebApplicationException e) {
			if (e.getResponse().getStatus() == Status.BAD_GATEWAY.getStatusCode()) {
				estimate.failure = "Beatmap file is corrupted. [https://github.com/ppy/osu-api/issues/130 reference]" + e.getResponse().getEntity();
				log.warn("Beatmap download failed: {}", e.getResponse().getEntity(), e);
				return estimate;
			}
			log.error("Beatmap download failed: {}", e.getResponse().getEntity(), e);
			estimate.failure = "Beatmap download failed " + e.getResponse() + "; " + e.getResponse().getEntity();
			return estimate;
		}

		SanDokuResponse sanDoku;
		try {
			sanDoku = calculator.getDiff(0, mods, actualBeatmap.getBytes(StandardCharsets.UTF_8));
			SanDokuDiffCalcResult r = sanDoku.diffCalcResult();
			if (r.sliderCount() + r.hitCircleCount() + r.spinnerCount() == 0) {
				estimate.failure = "Beatmap has no objects.";
				return estimate;
			}
		} catch (BadRequestException e) {
			estimate.failure = "SanDoku failed: " + SanDoku.unwrapError(e).map(Object.class::cast).orElseGet(e::getResponse);
			return estimate;
		} catch (InternalServerErrorException | ClientErrorException e) {
			estimate.failure = "SanDoku failed: " + e.getResponse();
			return estimate;
		}

		DiffEstimate.DiffEstimateToBeatmapImplMapper.INSTANCE.map(sanDoku.toBeatmap(), estimate);
		estimate.success = true;

		return estimate;
	}

	/**
	 * This is supposed to be called in a loop with a short delay for background maintenance.
	 * Once all beatmaps are up-to-date, the function will idle for an hour.
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

	/**
	 * @return true if everything is up-to-date, false if should be called again in a little while.
	 */
	boolean updateDiffEstimates() throws InterruptedException {
		try (Database db = dbm.getDatabase();
				Loader<DiffEstimate> loader = db.loader(DiffEstimate.class, "where `dataVersion` != ? limit 1")) {
			for (;;) {
				MDC.clear();
				Optional<DiffEstimate> outdated = loader.queryUnique(SanDoku.VERSION);
				if (!outdated.isPresent()) {
					return true;
				}

				DiffEstimate diffEstimate = outdated.get();
				MDC.put("beatmapid", String.valueOf(diffEstimate.beatmapid));
				System.out.printf("Maintenance: Updating diff estimate %s/%s%n", diffEstimate.beatmapid, diffEstimate.mods);
				loadOrCalculate(db, diffEstimate.beatmapid, diffEstimate.mods);
				if (MdcUtils.getInt(MdcUtils.MDC_EXTERNAL_API_CALLS).orElse(0) == 0) {
					// since we don't want to fire too many requests during maintenance, we break the loop here for a bit.
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
}
