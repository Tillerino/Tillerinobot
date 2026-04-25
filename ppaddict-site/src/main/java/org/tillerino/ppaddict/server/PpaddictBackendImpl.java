package org.tillerino.ppaddict.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.ppaddict.server.auth.Credentials;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.DiffEstimate;
import tillerino.tillerinobot.data.DiffEstimate.DiffEstimateToBeatmapImplMapper;
import tillerino.tillerinobot.diff.PercentageEstimatesImpl;

@Singleton
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PpaddictBackendImpl implements PpaddictBackend {
    private final DatabaseManager manager;

    private final OsuApi downloader;

    private final DiffEstimate.Repo diffEstimateRepo;

    private final ApiBeatmap.Repo apiBeatmapRepo;

    private final PpaddictCredentials.Repo credentialsRepo;

    @Override
    public PpaddictCredentials resolveCookie(String cookie) throws SQLException {
        try (Database db = manager.getDatabase()) {
            return credentialsRepo
                    .findByCookie(db, cookie, System.currentTimeMillis())
                    .orElse(null);
        }
    }

    @Override
    public String createCookie(Credentials userIdentifier) throws SQLException {
        try (Database db = manager.getDatabase()) {
            PpaddictCredentials pC = new PpaddictCredentials(userIdentifier);
            pC.setCookie(PpaddictCredentials.generateUniqueCookie(db));
            credentialsRepo.insert(db, pC);
            return pC.getCookie();
        }
    }

    final CompletableFuture<Map<BeatmapWithMods, BeatmapData>> cachedBeatmaps = new CompletableFuture<>();
    final AtomicLong beatmapsGeneration = new AtomicLong(-1);

    @Override
    public synchronized Map<BeatmapWithMods, BeatmapData> getBeatmaps() {
        try {
            return cachedBeatmaps.get();
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            log.error("error getting beatmaps", e);
            return null;
        }
    }

    private void loadAllBeatmaps(
            BiConsumer<BeatmapWithMods, BeatmapData> target, Map<BeatmapWithMods, BeatmapData> current)
            throws SQLException {
        log.debug("loading beatmaps");

        try (Database db = manager.getDatabase();
                Database secondConnection = manager.getDatabase()) {
            Map<Integer, OsuApiBeatmapForPpaddict> beatmaps = loadAllApiBeatmaps(secondConnection, current);
            List<Integer> deleteEstimates = new ArrayList<>();

            for (DiffEstimate estimate : diffEstimateRepo.iterateSuccessful(db)) {
                if (Thread.interrupted()) {
                    log.info("aborted loading beatmaps");
                    Thread.currentThread().interrupt();
                    break;
                }

                BeatmapWithMods key = new BeatmapWithMods(estimate.getBeatmapid(), estimate.getMods());
                OsuApiBeatmapForPpaddict apiBeatmap = beatmaps.get(key.beatmap());
                if (apiBeatmap == null) {
                    try {
                        apiBeatmap = OsuApiBeatmapForPpaddict.Mapper.INSTANCE.shrink(ApiBeatmap.loadOrDownload(
                                apiBeatmapRepo, secondConnection, estimate.getBeatmapid(), 0, 0, downloader));
                        if (apiBeatmap != null) {
                            // since this is very rare, we don't worry about de-duplicating strings here
                            beatmaps.put(key.beatmap(), apiBeatmap);
                        }
                    } catch (IOException e) {
                        log.error("error loading api beatmap", e);
                        continue;
                    }
                }

                if (apiBeatmap == null) {
                    log.warn("no api beatmap found for {}", estimate.getBeatmapid());
                    deleteEstimates.add(estimate.getBeatmapid());
                    continue;
                }

                target.accept(
                        key,
                        new BeatmapData(
                                new PercentageEstimatesImpl(
                                        DiffEstimateToBeatmapImplMapper.INSTANCE.map(estimate), estimate.getMods()),
                                apiBeatmap));
            }

            for (Integer beatmapId : deleteEstimates) {
                diffEstimateRepo.deleteByBeatmapId(db, beatmapId);
            }
        }
        log.debug("done loading beatmaps");
    }

    private Map<Integer, OsuApiBeatmapForPpaddict> loadAllApiBeatmaps(
            Database database, Map<BeatmapWithMods, BeatmapData> current) throws SQLException {
        Map<Integer, OsuApiBeatmapForPpaddict> beatmaps = new HashMap<>();
        Map<String, String> allStrings = new HashMap<>();
        UnaryOperator<String> stringDeduplicator = s -> s != null ? allStrings.computeIfAbsent(s, x -> x) : null;

        // de-duplicate with already existing strings for lower memory-footprint while reloading
        current.values().forEach(v -> {
            stringDeduplicator.apply(v.beatmap().getArtist());
            stringDeduplicator.apply(v.beatmap().getTitle());
            stringDeduplicator.apply(v.beatmap().getVersion());
        });

        apiBeatmapRepo.selectAllOsuNomod(database).forEach(b -> {
            // saves a loooooot of memory:
            // before we had: 293k OsuApiBeatmap instances (53MB) (one boxed field, all other fields strings)
            // 2.4M String instances (58MB)
            // 2.6M byte[] instances (145MB)
            // after we had: 291k OsuApiBeatmapForPpaddict instances (30MB)
            // 1M String instances (27MB)
            // 1M byte[] instances (44MB)
            OsuApiBeatmapForPpaddict shrunk = OsuApiBeatmapForPpaddict.Mapper.INSTANCE.shrink(b);
            BeatmapWithMods key = new BeatmapWithMods(b.getBeatmapId(), b.getMods());

            // replace with beatmap that is already in map to save memory while loading
            BeatmapData alreadyLoaded = current.get(key);
            if (alreadyLoaded != null && alreadyLoaded.beatmap().equals(shrunk)) {
                shrunk = alreadyLoaded.beatmap();
            }

            // saves even more memory:
            // approx 25% of versions are unique, 20% of titles, and 10% of artists.
            // putting all these in a hash map for this operation is _well worth it_.
            // before we had: 1M String intances (27MB)
            // 1M byte[] instances (44MB)
            // after we had: 267k String instances (6.4MB)
            // 288k byte[] instances (25MB)
            shrunk.setArtist(stringDeduplicator.apply(shrunk.getArtist()));
            shrunk.setTitle(stringDeduplicator.apply(shrunk.getTitle()));
            shrunk.setVersion(stringDeduplicator.apply(shrunk.getVersion()));

            beatmaps.put(key.beatmap(), shrunk);
        });
        return beatmaps;
    }

    public void scheduleUpdates(ScheduledExecutorService exec) {
        exec.scheduleWithFixedDelay(this::reloadBeatmaps, 0, 15, TimeUnit.MINUTES);
    }

    void reloadBeatmaps() {
        try {
            if (!cachedBeatmaps.isDone()) {
                // when ppaddict starts, we run the risk of blocking calls for a looooong time
                // this is especially crappy because the initial user data call waits for the beatmaps
                // so instead we just offer partially loaded data, but at least it's there :)
                Map<BeatmapWithMods, BeatmapData> fillslowly = new ConcurrentHashMap<>();
                cachedBeatmaps.complete(fillslowly);
                loadAllBeatmaps(fillslowly::put, Collections.emptyMap());
                beatmapsGeneration.set(0);
            } else {
                Map<BeatmapWithMods, BeatmapData> current = cachedBeatmaps.get();
                Map<BeatmapWithMods, BeatmapData> replace = new ConcurrentHashMap<>();
                loadAllBeatmaps(
                        (k, v) -> {
                            // put in current map to avoid using twice as much memory
                            current.put(k, v);
                            replace.put(k, v);
                        },
                        current);
                // replace to make sure that deletions take place
                cachedBeatmaps.obtrudeValue(replace);
                beatmapsGeneration.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("interrupted loading beatmaps", e);
        } catch (Exception e) {
            log.error("error loading beatmaps", e);
        }
    }

    @Override
    public long getBeatmapsGeneration() {
        return beatmapsGeneration.get();
    }
}
