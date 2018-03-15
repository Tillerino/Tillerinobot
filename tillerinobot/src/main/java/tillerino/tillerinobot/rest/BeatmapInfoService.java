package tillerino.tillerinobot.rest;
import static org.tillerino.osuApiModel.Mods.fixNC;
import static org.tillerino.osuApiModel.Mods.getEffectiveMods;
import static org.tillerino.osuApiModel.Mods.getMask;
import static org.tillerino.osuApiModel.Mods.getMods;

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
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotAPIServer;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;

@Singleton
@Path("/beatmapinfo")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BeatmapInfoService {
	private final BotBackend backend;
	private final ThreadLocalAutoCommittingEntityManager em;
	private final EntityManagerFactory emf;

	private final ExecutorService executorService = createExec();
	private static ExecutorService createExec() {
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 2,
				5L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		tpe.allowCoreThreadTimeOut(true);
		return tpe;
	}

	LoadingCache<BeatmapWithMods, Future<BeatmapMeta>> cache = CacheBuilder
			.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).softValues()
			.build(new CacheLoader<BeatmapWithMods, Future<BeatmapMeta>>() {
				@Override
				public Future<BeatmapMeta> load(final BeatmapWithMods key) {
					return executorService.submit(new Callable<BeatmapMeta>() {
						@Override
						public BeatmapMeta call() throws SQLException, InterruptedException {
							em.setThreadLocalEntityManager(emf.createEntityManager());
							try {
								BeatmapMeta beatmap = backend.loadBeatmap(
										key.getBeatmap(), key.getMods(),
										new Default());
								
								if(beatmap == null) {
									throw new NotFoundException("Beatmap " + key.getBeatmap() + " not found.");
								}
								
								return beatmap;
							} catch (IOException e) {
								throw BotAPIServer.getBadGateway(null);
							} catch (UserException e) {
								throw new NotFoundException(e.getMessage());
							} finally {
								em.close();
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
	
	@KeyRequired
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public BeatmapInfo getBeatmapInfo(@QueryParam("beatmapid") @BeatmapId int beatmapid, @QueryParam("mods") @BitwiseMods long mods, @QueryParam("acc") List<Double> requestedAccs, @QueryParam("wait") @DefaultValue("1000") long wait) throws Throwable {
		mods = fixNC(getMask(getEffectiveMods(getMods(mods))));

		BeatmapMeta beatmapMeta;

		try {
			Future<BeatmapMeta> future = cache.getUnchecked(new BeatmapWithMods(beatmapid, mods));
			beatmapMeta = wait < 0 ? future.get() : future.get(wait, TimeUnit.MILLISECONDS);
			
			PercentageEstimates estimates = beatmapMeta.getEstimates();

			BeatmapInfo info = new BeatmapInfo();
			info.beatmapid = beatmapid;
			info.mods = estimates.getMods();
			info.oppaiOnly = estimates.isOppaiOnly();
			info.starDiff = estimates.getStarDiff();

			if(requestedAccs.isEmpty()) {
				requestedAccs.addAll(Arrays.asList(1.0, .995, .99, .985, .98, .975, .97, .96, .95, .93, .9, .85, .8, .75));
			}

			for (double acc : requestedAccs) {
				info.ppForAcc.put(acc, estimates.getPP(acc));
			}

			return info;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw BotAPIServer.getInterrupted();
		} catch (ExecutionException e) {
			throw BotAPIServer.refreshWebApplicationException(e.getCause());
		} catch (TimeoutException e) {
			throw new WebApplicationException("The request is being processed. Please try again in a few moments.", Status.ACCEPTED);
		}
	}
}
