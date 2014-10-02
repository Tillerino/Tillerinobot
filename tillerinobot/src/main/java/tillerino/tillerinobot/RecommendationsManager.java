package tillerino.tillerinobot;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.lang.Language;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Communicates with the backend and creates recommendations samplers as well as caching information.
 * 
 * @author Tillerino
 */
@Singleton
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
		
		long[] getCauses();
		
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
	
	@Data
	public static class GivenRecommendation {
		public int userid;
		public String username;
		public int beatmapid;
		public long date;
		public long mods;
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
	public static class Sampler {
		final SortedMap<Double, BareRecommendation> distribution = new TreeMap<>();
		double sum = 0;
		final Random random = new Random();
		public final boolean nomod;
		public final Model type;
		public final long requestedMods;
		public Sampler(Collection<BareRecommendation> recommendations, Model type, boolean nomod, long requestedMods) {
			for (BareRecommendation bareRecommendation : recommendations) {
				sum += bareRecommendation.getProbability();
				distribution.put(sum, bareRecommendation);
			}
			
			this.nomod = nomod;
			this.type = type;
			this.requestedMods = requestedMods;
		}
		public boolean isEmpty() {
			return distribution.isEmpty();
		}
		public BareRecommendation sample() {
			double x = random.nextDouble() * sum;
			
			SortedMap<Double, BareRecommendation> rest = distribution.tailMap(x);
			if(rest.size() == 0) {
				// this means that there was some extreme numerical instability.
				// this is practically not possible. at least not *maximum stack
				// size* times in a row.
				return sample();
			}
			
			sum = rest.firstKey();
			
			BareRecommendation sample = rest.remove(sum);
			
			sum -= sample.getProbability();
			
			Collection<BareRecommendation> refill = new ArrayList<>();
			
			while(!rest.isEmpty()) {
				refill.add(rest.remove(rest.firstKey()));
			}
			
			for(BareRecommendation bareRecommendation : refill) {
				sum += bareRecommendation.getProbability();
				distribution.put(sum, bareRecommendation);
			}
			
			return sample;
		}
	}
	
	BotBackend backend;

	@Inject
	public RecommendationsManager(BotBackend backend) {
		this.backend = backend;
	}
	
	public Cache<String, Recommendation> lastRecommendation = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

	public LoadingCache<Integer, List<Integer>> givenRecomendations =  CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build(new CacheLoader<Integer, List<Integer>>() {
		@Override
		public List<Integer> load(Integer key) throws Exception {
			List<Integer> list = new ArrayList<>();
			for (GivenRecommendation recommendation : backend.loadGivenRecommendations(key)) {
				list.add(recommendation.getBeatmapid());
			}
			return list;
		}
	});
	
	/**
	 * These take long to calculate, so we want to keep them for a bit, but they also take a lot of space. 100 should be a good balance.
	 */
	public Cache<Integer, Sampler> samplers = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100).build();

	@CheckForNull
	public Recommendation getLastRecommendation(String nick) {
		return lastRecommendation.getIfPresent(nick);
	}

	/**
	 * get an ready-to-display recommendation
	 * @param ircName for legacy reasons, the IRC name is used to save recommendations
	 * @param message the remaining arguments ("r" or "recommend" were removed)
	 * @param lang TODO
	 * @return
	 * @throws UserException
	 * @throws SQLException
	 * @throws IOException
	 */
	public Recommendation getRecommendation(String ircName, @Nonnull OsuApiUser apiUser, String message, Language lang) throws UserException, SQLException, IOException {
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
			String param = remaining[i];
			if(param.length() == 0)
				continue;
			if(getLevenshteinDistance(param, "nomod") <= 2) {
				nomod = true;
				continue;
			}
			if(getLevenshteinDistance(param, "relax") <= 2) {
				model = Model.ALPHA;
				continue;
			}
			if(getLevenshteinDistance(param, "gamma") <= 2) {
				model = Model.GAMMA;
				continue;
			}
			if(model == Model.GAMMA && param.equals("dt")) {
				requestMods |= Mods.getMask(Mods.DoubleTime);
				continue;
			}
			if(model == Model.GAMMA &&  param.equals("hr")) {
				requestMods |= Mods.getMask(Mods.HardRock);
				continue;
			}
			throw new UserException(lang.unknownRecommendationParameter(param));
		}
		
		/*
		 * verify the arguments
		 */
		
		if(nomod && requestMods != 0) {
			throw new UserException(lang.mixedNomodAndMods());
		}
		
		if(model == Model.GAMMA) {
			int minRank = 25000;
			if(apiUser.getRank() > minRank) {
				apiUser = backend.getUser(userid, 1);
				
				if(apiUser == null) {
					throw new RuntimeException("trolled by the API? " + userid);
				}
				
				if(apiUser.getRank() > minRank) {
					throw new UserException(lang.featureRankRestricted("gamma", minRank, apiUser));
				}
			}
		}
		
		/*
		 * load sampler
		 */
		
		Sampler sampler = samplers.getIfPresent(userid);
		
		if(sampler == null || sampler.nomod != nomod || sampler.type != model || sampler.requestedMods != requestMods) {
			Collection<BareRecommendation> recommendations = backend.loadRecommendations(userid, givenRecomendations.getUnchecked(userid), model, nomod, requestMods);
			
			// only keep the 1k most probable recommendations to save some memory
			recommendations = getTopRecommendations(recommendations);
			
			sampler = new Sampler(recommendations, model, nomod, requestMods);
			
			samplers.put(userid, sampler);
		}
		
		if(sampler.isEmpty()) {
			samplers.invalidate(userid);
			givenRecomendations.put(userid, new ArrayList<Integer>());
			lastRecommendation.invalidate(ircName);
			throw new UserException(lang.outOfRecommendations());
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
				loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
			} else {
				loadBeatmap = backend.loadBeatmap(beatmapid, sample.getMods(), lang);
				if(loadBeatmap == null)
					loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
			}
		} catch (NotRankedException e) {
			throw new UserException(lang.excuseForError());
		}
		recommendation.beatmap = loadBeatmap;
		
		loadBeatmap.setPersonalPP(sample.getPersonalPP());
		
		/*
		 * save recommendation internally
		 */
		
		givenRecomendations.getUnchecked(userid).add(beatmapid);
		lastRecommendation.put(ircName, recommendation);
		
		return recommendation;
	}

	/**
	 * returns
	 * @param recommendations
	 * @return
	 */
	public static List<BareRecommendation> getTopRecommendations(Collection<BareRecommendation> recommendations) {
		List<BareRecommendation> list = new ArrayList<>(recommendations);
		
		Collections.sort(list, new Comparator<BareRecommendation>() {
			@Override
			public int compare(BareRecommendation o1, BareRecommendation o2) {
				return (int) Math.signum(o2.getProbability() - o1.getProbability());
			}
		});

		int size = Math.min(list.size(), 1000);
		ArrayList<BareRecommendation> arrayList = new ArrayList<>(size);
		arrayList.addAll(list.subList(0, size));
		
		return arrayList;
	}
}
