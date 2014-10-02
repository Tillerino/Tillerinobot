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

import org.apache.commons.lang3.tuple.Pair;

import static org.tillerino.osuApiModel.Mods.*;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.Data;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotAPIServer;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
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
	
	LoadingCache<Pair<Integer, Long>, Future<BeatmapMeta>> cache = CacheBuilder
			.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).softValues()
			.build(new CacheLoader<Pair<Integer, Long>, Future<BeatmapMeta>>() {
				@Override
				public Future<BeatmapMeta> load(final Pair<Integer, Long> key) {
					return executorService.submit(new Callable<BeatmapMeta>() {
						@Override
						public BeatmapMeta call() throws SQLException {
							try {
								BeatmapMeta beatmap = backend.loadBeatmap(
										key.getKey(), key.getValue(),
										new Default());
								
								if(beatmap == null) {
									throw BotAPIServer.getNotFound("Beatmap " + key.getKey() + " not found.");
								}
								
								return beatmap;
							} catch (IOException e) {
								throw BotAPIServer.getBadGateway();
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
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public BeatmapInfo getBeatmapInfo(@QueryParam("k") String key, @QueryParam("beatmapid") int beatmapid, @QueryParam("mods") long mods, @QueryParam("wait") @DefaultValue("1000") long wait) throws Throwable {
		BotAPIServer.throwUnautorized(backend.verifyGeneralKey(key));
		
		mods = fixNC(getMask(getEffectiveMods(getMods(mods))));
		
		BeatmapMeta beatmapMeta;

		try {
			Future<BeatmapMeta> future = cache.getUnchecked(Pair.of(beatmapid, mods));
			beatmapMeta = wait < 0 ? future.get() : future.get(wait, TimeUnit.MILLISECONDS);
			
			if (beatmapMeta.getEstimates() instanceof PercentageEstimates) {
				PercentageEstimates estimates = (PercentageEstimates) beatmapMeta.getEstimates();
				
				BeatmapInfo info = new BeatmapInfo();
				info.beatmapid = beatmapid;
				info.mods = estimates.getMods();
				
				for(double acc : new double[] { 1, .995, .99, .985, .98, .975, .97, .96, .95, .93, .9, .85, .8, .75 }) {
					info.ppForAcc.put(acc, estimates.getPPForAcc(acc));
				}
				
				return info;
			}
			
			throw BotAPIServer.getNotFound("Percentages estimates not found.");
		} catch (InterruptedException e) {
			throw BotAPIServer.getInterrupted();
		} catch (ExecutionException e) {
			throw BotAPIServer.refreshWebApplicationException(e.getCause());
		} catch (TimeoutException e) {
			throw BotAPIServer.exceptionFor(Status.ACCEPTED, "The request is being processed. Please try again in a few moments.");
		}
	}
}
