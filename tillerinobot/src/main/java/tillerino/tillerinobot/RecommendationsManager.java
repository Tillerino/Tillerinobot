package tillerino.tillerinobot;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Communicates with the backend and creates recommendations samplers as well as caching information.
 * 
 * @author Tillerino
 */
public class RecommendationsManager {
	/**
	 * Recommendation as returned by the backend. Needs to be enriched before being displayed.
	 * 
	 * @author Tillerino
	 */
	public interface BareRecommendation {
		int getBeatmapId();
		
		/**
		 * mods for this recommendation
		 * @return 0 for no mods, -1 for unknown mods, any other long for mods according to {@link Mods}
		 */
		long getMods();
		
		Collection<Long> getCauses();
		
		/**
		 * returns a guess at how much pp the player could achieve for this recommendation
		 * @return null if no personal pp were calculated
		 */
		Integer getPersonalPP();
		
		/**
		 * @return this is not normed, so the sum of all probabilities can be greater than 1 and this must be accounted for!
		 */
		double getProbability();
	}
	
	/**
	 * Enriched Recommendation.
	 * 
	 * @author Tillerino
	 */
	public static class Recommendation {
		public BeatmapMeta beatmap;
		
		public BareRecommendation bareRecommendation;
	}
	
	/**
	 * The type of recommendation model that the player has chosen.
	 * 
	 * @author Tillerino
	 */
	public enum Model {
		ALPHA,
		BETA,
		GAMMA
	}
	
	/**
	 * Thrown when a loaded beatmap has neither aproved status 1 or 2.
	 * 
	 *	// TODO find out about "qualified" = 3
	 * 
	 * @author Tillerino
	 */
	public static class NotRankedException extends UserException {
		private static final long serialVersionUID = 1L;

		public NotRankedException(String message) {
			super(message);
		}
	}
	
	/**
	 * Distribution for recommendations. Changes upon sampling.
	 * 
	 * @author Tillerino
	 */
	public class Sampler {
		final SortedMap<Double, BareRecommendation> distribution = new TreeMap<>();
		double sum = 0;
		final Random random = new Random();
		public final boolean nomod;
		public final Model type;
		public final long requestedMods;
		public Sampler(Collection<BareRecommendation> recommendations, Model type, boolean nomod, long requestedMods) {
			for (BareRecommendation bareRecommendation : recommendations) {
				distribution.put(sum, bareRecommendation);
				sum += bareRecommendation.getProbability();
			}
			
			this.nomod = nomod;
			this.type = type;
			this.requestedMods = requestedMods;
		}
		public boolean isEmpty() {
			return distribution.isEmpty();
		}
		public BareRecommendation sample() {
			SortedMap<Double, BareRecommendation> rest = distribution.tailMap(random.nextDouble() * sum);
			if(rest.size() == 0) {
				// this means that there was some extreme numerical instability.
				// this is practically not possible. at least not *maximum stack
				// size* times in a row.
				return sample();
			}
			sum -= rest.firstKey();
			return rest.remove(rest.firstKey());
		}
	}
	
	BotBackend backend;

	public RecommendationsManager(BotBackend backend) {
		this.backend = backend;
	}
	
	public Cache<String, Recommendation> lastRecommendation = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

	public LoadingCache<String, Set<Integer>> givenRecomendations =  CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build(new CacheLoader<String, Set<Integer>>() {
		@Override
		public Set<Integer> load(String key) throws Exception {
			return backend.loadGivenRecommendations(key);
		}
	});
	
	public Cache<Integer, Sampler> samplers = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

	@CheckForNull
	public Recommendation getLastRecommendation(String nick) {
		return lastRecommendation.getIfPresent(nick);
	}

	/**
	 * get an ready-to-display recommendation
	 * @param ircName for legacy reasons, the IRC name is used to save recommendations
	 * @param message the remaining arguments ("r" or "recommend" were removed)
	 * @return
	 * @throws UserException
	 * @throws SQLException
	 * @throws IOException
	 */
	public Recommendation getRecommendation(String ircName, @Nonnull OsuApiUser apiUser, String message) throws UserException, SQLException, IOException {
		/*
		 * log activity making sure that we can resolve the user's IRC name
		 */
		
		Integer userid = apiUser.getUserId();
		
		backend.registerActivity(userid);
		
		/*
		 * parse arguments
		 */
		
		String[] remaining = message.split(" ");
		
		boolean nomod = false;
		Model model = Model.BETA;
		long requestMods = 0;
		
		for (int i = 0; i < remaining.length; i++) {
			if(remaining[i].length() == 0)
				continue;
			if(getLevenshteinDistance(remaining[i], "nomod") <= 2) {
				nomod = true;
				continue;
			}
			if(getLevenshteinDistance(remaining[i], "relax") <= 2) {
				model = Model.ALPHA;
				continue;
			}
			if(getLevenshteinDistance(remaining[i], "gamma") <= 2) {
				model = Model.GAMMA;
				continue;
			}
			if(model == Model.GAMMA && remaining[i].equals("dt")) {
				requestMods |= Mods.getMask(Mods.DoubleTime);
				continue;
			}
			if(model == Model.GAMMA &&  remaining[i].equals("hr")) {
				requestMods |= Mods.getMask(Mods.HardRock);
				continue;
			}
			throw new UserException("I don't know what \"" + remaining[i] + "\" is supposed to mean. Try !help if you need some pointers.");
		}
		
		/*
		 * verify the arguments
		 */
		
		if(nomod && requestMods != 0) {
			throw new UserException("nomod with mods?");
		}
		
		if(model == Model.GAMMA) {
			if(apiUser.getRank() > 15000) {
				apiUser = backend.getUser(userid, 1);
				
				if(apiUser == null) {
					throw new RuntimeException("trolled by the API? " + userid);
				}
				
				if(apiUser.getRank() > 15000) {
					throw new UserException("Sorry, at this point gamma recommendations are only available for players who have surpassed rank 15k.");
				}
			}
		}
		
		/*
		 * load sampler
		 */
		
		Sampler sampler = samplers.getIfPresent(userid);
		
		if(sampler == null || sampler.nomod != nomod || sampler.type != model || sampler.requestedMods != requestMods) {
			sampler = new Sampler(backend.loadRecommendations(userid, givenRecomendations.getUnchecked(ircName), model, nomod, requestMods), model, nomod, requestMods);
			
			samplers.put(userid, sampler);
		}
		
		if(sampler.isEmpty()) {
			samplers.invalidate(userid);
			givenRecomendations.put(ircName, new HashSet<Integer>());
			lastRecommendation.invalidate(ircName);
			throw new UserException("I've recommended everything that I can think of. Try again to start over!");
		}
		
		/*
		 * sample and load meta data
		 */
		
		BareRecommendation sample = sampler.sample();
		
		int beatmapid = sample.getBeatmapId();
		
		Recommendation recommendation = new Recommendation();
		
		recommendation.bareRecommendation = sample;
		
		BeatmapMeta loadBeatmap;
		try {
			if(sample.getMods() < 0) {
				loadBeatmap = backend.loadBeatmap(beatmapid, 0);
			} else {
				loadBeatmap = backend.loadBeatmap(beatmapid, sample.getMods());
				if(loadBeatmap == null)
					loadBeatmap = backend.loadBeatmap(beatmapid, 0);
			}
		} catch (NotRankedException e) {
			throw new UserException("I'm sorry, I wasn't paying attention. Could you repeat that?");
		}
		recommendation.beatmap = loadBeatmap;
		
		loadBeatmap.setPersonalPP(sample.getPersonalPP());
		
		/*
		 * save recommendation internally
		 */
		
		givenRecomendations.getUnchecked(ircName).add(beatmapid);
		lastRecommendation.put(ircName, recommendation);
		
		return recommendation;
	}
}
