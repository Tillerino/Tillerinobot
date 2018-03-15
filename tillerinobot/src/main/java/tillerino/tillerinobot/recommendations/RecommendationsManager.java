package tillerino.tillerinobot.recommendations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.data.repos.GivenRecommendationRepository;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.RecommendationPredicate;

/**
 * Communicates with the backend and creates recommendations samplers as well as caching information.
 * 
 * @author Tillerino
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecommendationsManager {
	private final BotBackend backend;
	
	private final GivenRecommendationRepository recommendationsRepo;
	
	private final ThreadLocalAutoCommittingEntityManager em;

	private final RecommendationRequestParser parser;

	private final Cache<Integer, Recommendation> lastRecommendation = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.build();

	private final LoadingCache<Integer, List<GivenRecommendation>> givenRecomendations = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build(CacheLoader.from(this::doLoadGivenRecommendations));
	
	/**
	 * These take long to calculate, so we want to keep them for a bit, but they also take a lot of space. 100 should be a good balance.
	 */
	private final Cache<Integer, Sampler<BareRecommendation, RecommendationRequest>> samplers = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.maximumSize(100)
			.build();

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

		Sampler<BareRecommendation, RecommendationRequest> sampler = samplers.getIfPresent(userid);

		if (sampler == null || message != null) {
			/*
			 * parse arguments
			 */

			RecommendationRequest settings = parseSamplerSettings(apiUser, message == null ? "" : message, lang);

			if (sampler == null || !sampler.getSettings().equals(settings)) {
				List<Integer> exclude = loadGivenRecommendations(userid)
						.stream().map(GivenRecommendation::getBeatmapid)
						.collect(Collectors.toList());
				Collection<BareRecommendation> recommendations = backend
						.loadRecommendations(userid, exclude, settings.getModel(),
								settings.isNomod(), settings.getRequestedMods());

				// only keep the 1k most probable recommendations to save some
				// memory
				recommendations = getTopRecommendations(recommendations, settings.getPredicates());

				sampler = new Sampler<>(recommendations, settings, BareRecommendation::getProbability);

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

	public RecommendationRequest parseSamplerSettings(OsuApiUser apiUser, @Nonnull String message,
			Language lang) throws UserException, SQLException, IOException {
		return parser.parseSamplerSettings(apiUser, message, lang);
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

		Collections.sort(list, Comparator.comparingDouble(BareRecommendation::getProbability).reversed());

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
	public void hideRecommendation(@UserId int userId, @BeatmapId int beatmapid, @BitwiseMods long mods) {
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
