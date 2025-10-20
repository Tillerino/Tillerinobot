package tillerino.tillerinobot.rest;

import static org.tillerino.osuApiModel.Mods.fixNC;
import static org.tillerino.osuApiModel.Mods.getEffectiveMods;
import static org.tillerino.osuApiModel.Mods.getMask;
import static org.tillerino.osuApiModel.Mods.getMods;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BeatmapInfoService implements BeatmapDifficulties {
    private final BotBackend backend;

    private final ExecutorService executorService = createExec();

    private static ExecutorService createExec() {
        ThreadPoolExecutor tpe =
                new ThreadPoolExecutor(2, 2, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        tpe.allowCoreThreadTimeOut(true);
        return tpe;
    }

    LoadingCache<BeatmapWithMods, Future<BeatmapMeta>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .softValues()
            .build(new CacheLoader<BeatmapWithMods, Future<BeatmapMeta>>() {
                @Override
                public Future<BeatmapMeta> load(final BeatmapWithMods key) {
                    return executorService.submit(new Callable<BeatmapMeta>() {
                        @Override
                        public BeatmapMeta call() throws SQLException, InterruptedException {
                            try {
                                BeatmapMeta beatmap = backend.loadBeatmap(key.beatmap(), key.mods(), new Default());

                                if (beatmap == null) {
                                    throw new NotFoundException("Beatmap " + key.beatmap() + " not found.");
                                }

                                return beatmap;
                            } catch (IOException e) {
                                throw RestUtils.getBadGateway(null);
                            } catch (UserException e) {
                                throw new NotFoundException(e.getMessage());
                            }
                        }
                    });
                }
            });

    @Data
    public static class BeatmapInfo {
        int beatmapid;
        long mods;
        Map<Double, Double> ppForAcc = new TreeMap<>();
        boolean oppaiOnly;
        Double starDiff;
    }

    @Override
    public BeatmapInfo getBeatmapInfo(
            @BeatmapId int beatmapid, @BitwiseMods long mods, List<Double> requestedAccs, long wait) throws Throwable {
        mods = fixNC(getMask(getEffectiveMods(getMods(mods))));

        BeatmapMeta beatmapMeta;

        try {
            Future<BeatmapMeta> future = cache.getUnchecked(new BeatmapWithMods(beatmapid, mods));
            beatmapMeta = wait < 0 ? future.get() : future.get(wait, TimeUnit.MILLISECONDS);

            PercentageEstimates estimates = beatmapMeta.getEstimates();

            BeatmapInfo info = new BeatmapInfo();
            info.beatmapid = beatmapid;
            info.mods = estimates.getMods();
            info.oppaiOnly = false;
            info.starDiff = estimates.getStarDiff();

            if (requestedAccs.isEmpty()) {
                requestedAccs.addAll(
                        Arrays.asList(1.0, .995, .99, .985, .98, .975, .97, .96, .95, .93, .9, .85, .8, .75));
            }

            for (double acc : requestedAccs) {
                info.ppForAcc.put(acc, estimates.getPP(acc));
            }

            return info;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw RestUtils.getInterrupted();
        } catch (ExecutionException e) {
            throw RestUtils.refreshWebApplicationException(e.getCause());
        } catch (TimeoutException e) {
            throw new WebApplicationException(
                    "The request is being processed. Please try again in a few moments.", Status.ACCEPTED);
        }
    }
}
