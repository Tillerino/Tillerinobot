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
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.RecommendationsManager.Sampler.Settings;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.PredicateParser;
import tillerino.tillerinobot.predicates.RecommendationPredicate;

/**
 * Communicates with the backend and creates recommendations samplers as well as caching information.
 * 
 * @author Tillerino
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecommendationsManager {
	/**
	 * Recommendation as returned by the backend. Needs to be enriched before being displayed.
	 * 
	 * @author Tillerino
	 */
	public interface BareRecommendation {
		@BeatmapId
		int getBeatmapId();
		
		/**
		 * mods for this recommendation
		 * @return 0 for no mods, -1 for unknown mods, any other long for mods according to {@link Mods}
		 */
		@BitwiseMods
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
	
	/**
	 * Enriched Recommendation.
	 * 
	 * @author Tillerino
	 */
	@RequiredArgsConstructor
	public static class Recommendation {
		public final BeatmapMeta beatmap;
		
		public final BareRecommendation bareRecommendation;
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
		@EqualsAndHashCode
		public static class Settings {
			public boolean nomod;
			public Model model;
			@BitwiseMods
			public long requestedMods;
			public List<RecommendationPredicate> predicates = new ArrayList<>();
		}
		final SortedMap<Double, BareRecommendation> distribution = new TreeMap<>();
		double sum = 0;
		final Random random = new Random();
		final Settings settings;
		public Sampler(Collection<BareRecommendation> recommendations, Settings settings) {
			for (BareRecommendation bareRecommendation : recommendations) {
				sum += bareRecommendation.getProbability();
				distribution.put(sum, bareRecommendation);
			}
			this.settings = settings;
		}
		public boolean isEmpty() {
			return distribution.isEmpty();
		}
		public synchronized BareRecommendation sample() {
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
	
	private final BotBackend backend;
	
	private final GivenRecommendationRepository recommendationsRepo;
	
	private final ThreadLocalAutoCommittingEntityManager em;

	PredicateParser parser = new PredicateParser();

	public Cache<Integer, Recommendation> lastRecommendation = CacheBuilder
			.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).recordStats().build();

	public LoadingCache<Integer, List<GivenRecommendation>> givenRecomendations = CacheBuilder
			.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).recordStats()
			.build(CacheLoader.from(this::doLoadGivenRecommendations));
	
	/**
	 * These take long to calculate, so we want to keep them for a bit, but they also take a lot of space. 100 should be a good balance.
	 */
	public Cache<Integer, Sampler> samplers = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100).build();

	@CheckForNull
	public Recommendation getLastRecommendation(Integer userid) {
		return lastRecommendation.getIfPresent(userid);
	}

	/**
	 * get an ready-to-display recommendation
	 * 
	 * @param apiUser
	 * @param message
	 *            the remaining arguments ("r" or "recommend" were removed).
	 *            null if an existing sampler should be reused.
	 * @param lang
	 * @return
	 * @throws UserException
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public Recommendation getRecommendation(@Nonnull OsuApiUser apiUser, @CheckForNull String message, Language lang)
			throws UserException, SQLException, IOException, InterruptedException {
		int userid = apiUser.getUserId();
		/*
		 * load sampler
		 */

		Sampler sampler = samplers.getIfPresent(userid);

		if (sampler == null || message != null) {
			/*
			 * parse arguments
			 */

			Settings settings = parseSamplerSettings(apiUser, message == null ? "" : message, lang);

			if (sampler == null || !sampler.settings.equals(settings)) {
				List<Integer> exclude = loadGivenRecommendations(userid)
						.stream().map(GivenRecommendation::getBeatmapid)
						.collect(Collectors.toList());
				Collection<BareRecommendation> recommendations = backend
						.loadRecommendations(userid, exclude, settings.model,
								settings.nomod, settings.requestedMods);

				// only keep the 1k most probable recommendations to save some
				// memory
				recommendations = getTopRecommendations(recommendations, settings.predicates);

				sampler = new Sampler(recommendations, settings);

				samplers.put(userid, sampler);
			}
		}

		if (sampler.isEmpty()) {
			samplers.invalidate(userid);
			throw new UserException(lang.outOfRecommendations());
		}

		/*
		 * sample and load meta data
		 */

		BareRecommendation sample = sampler.sample();

		int beatmapid = sample.getBeatmapId();

		BeatmapMeta loadBeatmap;
		try {
			if (sample.getMods() < 0) {
				loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
			} else {
				loadBeatmap = backend.loadBeatmap(beatmapid, sample.getMods(),
						lang);
				if (loadBeatmap == null)
					loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
			}
		} catch (NotRankedException e) {
			throw new RareUserException(lang.excuseForError());
		}
		if (loadBeatmap == null) {
			throw new RareUserException(lang.excuseForError());
		}
		loadBeatmap.setPersonalPP(sample.getPersonalPP());

		/*
		 * save recommendation internally
		 */

		Recommendation recommendation = new Recommendation(loadBeatmap, sample);
		
		loadGivenRecommendations(userid).add(toGivenRecommendation(sample, userid));
		lastRecommendation.put(userid, recommendation);

		return recommendation;
	}

	private GivenRecommendation toGivenRecommendation(
			BareRecommendation sample, @UserId int userid) {
		return new GivenRecommendation(userid, sample.getBeatmapId(),
				System.currentTimeMillis(), sample.getMods());
	}

	public Settings parseSamplerSettings(OsuApiUser apiUser, @Nonnull String message,
			Language lang) throws UserException, SQLException, IOException {
		String[] remaining = message.split(" ");
		
		Settings settings = new Settings();
		
		settings.model = Model.GAMMA;

		for (int i = 0; i < remaining.length; i++) {
			String param = remaining[i];
			String lowerCase = param.toLowerCase();
			if(lowerCase.length() == 0)
				continue;
			if(getLevenshteinDistance(lowerCase, "nomod") <= 2) {
				settings.nomod = true;
				continue;
			}
			if(getLevenshteinDistance(lowerCase, "relax") <= 2) {
				settings.model = Model.ALPHA;
				continue;
			}
			if(getLevenshteinDistance(lowerCase, "beta") <= 1) {
				settings.model = Model.BETA;
				continue;
			}
			if(getLevenshteinDistance(lowerCase, "gamma") <= 2) {
				settings.model = Model.GAMMA;
				continue;
			}
			if(settings.model == Model.GAMMA && (lowerCase.equals("dt") || lowerCase.equals("nc"))) {
				settings.requestedMods = Mods.add(settings.requestedMods, Mods.DoubleTime);
				continue;
			}
			if(settings.model == Model.GAMMA &&  lowerCase.equals("hr")) {
				settings.requestedMods = Mods.add(settings.requestedMods, Mods.HardRock);
				continue;
			}
			if(settings.model == Model.GAMMA &&  lowerCase.equals("hd")) {
				settings.requestedMods = Mods.add(settings.requestedMods, Mods.Hidden);
				continue;
			}
			if (settings.model == Model.GAMMA) {
				Long mods = Mods.fromShortNamesContinuous(lowerCase);
				if (mods != null) {
					mods = Mods.fixNC(mods);
					if (mods == (mods & Mods.getMask(Mods.DoubleTime, Mods.HardRock, Mods.Hidden))) {
						for (Mods mod : Mods.getMods(mods)) {
							settings.requestedMods = Mods.add(settings.requestedMods, mod);
						}
						continue;
					}
				}
			}
			if (backend.getDonator(apiUser) > 0) {
				RecommendationPredicate predicate = parser.tryParse(param, lang);
				if (predicate != null) {
					for (RecommendationPredicate existingPredicate : settings.predicates) {
						if (existingPredicate.contradicts(predicate)) {
							throw new UserException(lang.invalidChoice(
									existingPredicate.getOriginalArgument() + " with "
											+ predicate.getOriginalArgument(),
									"either " + existingPredicate.getOriginalArgument() + " or "
											+ predicate.getOriginalArgument()));
						}
					}
					settings.predicates.add(predicate);
					continue;
				}
			}
			throw new UserException(lang.invalidChoice(param,
					"[nomod] [relax|beta|gamma] [dt] [hr] [hd]"));
		}
		
		/*
		 * verify the arguments
		 */
		
		if(settings.nomod && settings.requestedMods != 0) {
			throw new UserException(lang.mixedNomodAndMods());
		}
		
		return settings;
	}

	/**
	 * returns
	 * 
	 * @param recommendations
	 * @param predicates
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public List<BareRecommendation> getTopRecommendations(
			Collection<BareRecommendation> recommendations,
			List<RecommendationPredicate> predicates) throws SQLException, IOException {
		List<BareRecommendation> list = new ArrayList<>();

		recommendationsLoop: for (BareRecommendation bareRecommendation : recommendations) {
			OsuApiBeatmap beatmap = null;
			for (RecommendationPredicate predicate : predicates) {
				if (beatmap == null) {
					beatmap = backend.getBeatmap(bareRecommendation.getBeatmapId());
				}
				if (!predicate.test(bareRecommendation, beatmap)) {
					continue recommendationsLoop;
				}
			}
			list.add(bareRecommendation);
		}
		
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

	/**
	 * forgets all given recommendations of the past for a single user
	 * 
	 * @param user
	 */
	public void forgetRecommendations(@UserId int user) {
		em.ensureTransaction(() -> recommendationsRepo.forgetAll(user));
		givenRecomendations.getUnchecked(user).clear();
	}
	
	public void saveGivenRecommendation(@UserId int userid,
			@BeatmapId int beatmapid, @BitwiseMods long mods) {
		GivenRecommendation givenRecommendation = new GivenRecommendation(
				userid, beatmapid, System.currentTimeMillis(), mods);
		recommendationsRepo.save(givenRecommendation);
	}
	
	/**
	 * recommendations from the last four weeks
	 * @param userid
	 * @return ordered by date given from newest to oldest
	 */
	public List<GivenRecommendation> loadGivenRecommendations(@UserId int userid) {
		return givenRecomendations.getUnchecked(userid);
	}

	private List<GivenRecommendation> doLoadGivenRecommendations(@UserId int userid) {
		// we have to make a copy here since this list will escape the current entity manager
		return new ArrayList<>(recommendationsRepo
				.findByUseridAndDateGreaterThanAndForgottenFalseOrderByDateDesc(
						userid, System.currentTimeMillis() - 28l * 24 * 60 * 60
								* 1000));
	}
	
	/**
	 * Hide a recommendation. It will no longer be displayed in ppaddict, but
	 * still taken into account when generating new recommendations.
	 */
	public void hideRecommendation(@UserId int userId,
			@BeatmapId int beatmapid, @BitwiseMods long mods)
			throws SQLException {
		em.ensureTransaction(() -> recommendationsRepo.hideRecommendations(userId, beatmapid, mods));
	}

	/**
	 * non-hidden recommendations. These might be older than four weeks.
	 * @returnordered by date given from newest to oldest
	 */
	public List<GivenRecommendation> loadVisibleRecommendations(@UserId int userId) {
		return recommendationsRepo.findByUseridAndHiddenFalseOrderByDateDesc(userId);
	}
}
