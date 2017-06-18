package tillerino.tillerinobot.rest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

import static org.tillerino.osuApiModel.Mods.*;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Data;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotAPIServer;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;

@Singleton
@Path("/beatmapinfo")
public class BeatmapInfoService {
	private BotBackend backend;

	@Inject
	public BeatmapInfoService(BotBackend server) {
		this.backend = server;
	}

	ExecutorService executorService = Executors.newFixedThreadPool(2);
	
	LoadingCache<BeatmapWithMods, Future<BeatmapMeta>> cache = CacheBuilder
			.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).softValues()
			.build(new CacheLoader<BeatmapWithMods, Future<BeatmapMeta>>() {
				@Override
				public Future<BeatmapMeta> load(final BeatmapWithMods key) {
					return executorService.submit(new Callable<BeatmapMeta>() {
						@Override
						public BeatmapMeta call() throws SQLException, InterruptedException {
							try {
								BeatmapMeta beatmap = backend.loadBeatmap(
										key.getBeatmap(), key.getMods(),
										new Default());
								
								if(beatmap == null) {
									throw BotAPIServer.getNotFound("Beatmap " + key.getBeatmap() + " not found.");
								}
								
								return beatmap;
							} catch (IOException e) {
								throw BotAPIServer.getBadGateway(null);
							} catch (UserException e) {
								throw BotAPIServer.getUserMessage(e);
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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public BeatmapInfo getBeatmapInfo(@QueryParam("k") String key, @QueryParam("beatmapid") @BeatmapId int beatmapid, @QueryParam("mods") @BitwiseMods long mods, @QueryParam("acc") Double requestedAcc, @QueryParam("wait") @DefaultValue("1000") long wait) throws Throwable {
		BotAPIServer.throwUnautorized(backend.verifyGeneralKey(key));
		
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

			if(requestedAcc == null) {
				for (double acc : new double[]{1, .995, .99, .985, .98, .975, .97, .96, .95, .93, .9, .85, .8, .75}) {
					info.ppForAcc.put(acc, estimates.getPPForAcc(acc));
				}
			} else {
				info.ppForAcc.put(requestedAcc, estimates.getPPForAcc(requestedAcc));
			}
			
			return info;
		} catch (InterruptedException e) {
			throw BotAPIServer.getInterrupted();
		} catch (ExecutionException e) {
			throw BotAPIServer.refreshWebApplicationException(e.getCause());
		} catch (TimeoutException e) {
			throw BotAPIServer.exceptionFor(Status.ACCEPTED, "The request is being processed. Please try again in a few moments.");
		}
	}
}
