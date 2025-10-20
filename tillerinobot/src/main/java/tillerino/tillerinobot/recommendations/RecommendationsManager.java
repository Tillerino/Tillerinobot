package tillerino.tillerinobot.recommendations;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.tillerino.osuApiModel.Mods.Nightcore;
import static org.tillerino.osuApiModel.Mods.getEffectiveMods;
import static org.tillerino.osuApiModel.Mods.getMask;
import static org.tillerino.osuApiModel.Mods.getMods;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.config.ConfigService;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.BotBackend.BeatmapsLoader;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.UserException.RareUserException;
import tillerino.tillerinobot.data.GivenRecommendation;
import tillerino.tillerinobot.data.Player;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.predicates.RecommendationPredicate;
import tillerino.tillerinobot.recommendations.RecommendationRequest.Shift;

/**
 * Communicates with the backend and creates recommendations samplers as well as caching information.
 *
 * @author Tillerino
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RecommendationsManager {
    private final BotBackend backend;
    private final DatabaseManager dbm;
    private final RecommendationRequestParser parser;
    private final BeatmapsLoader beatmapsLoader;
    private final Recommender recommender;
    private final OsuApi osuApi;
    private final Clock clock;
    private final ConfigService config;

    private final Cache<Integer, Recommendation> lastRecommendation =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    /**
     * These take long to calculate, so we want to keep them for a bit, but they also take a lot of space. 100 should be
     * a good balance.
     */
    private final Cache<Integer, Sampler<BareRecommendation, RecommendationRequest>> samplers =
            CacheBuilder.newBuilder()
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
     * @param message the remaining arguments ("r" or "recommend" were removed). null if an existing sampler should be
     *     reused.
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
                try (var _ = PhaseTimer.timeTask("loadSampler")) {
                    sampler = loadSampler(userid, settings);
                }

                samplers.put(userid, sampler);
            }
        }

        MDC.put(MdcUtils.MDC_ENGINE, sampler.getSettings().model().toString());

        if (sampler.isEmpty()) {
            samplers.invalidate(userid);
            throw new UserException(lang.outOfRecommendations());
        }

        /*
         * sample and load meta data
         */

        BareRecommendation sample;
        try (var _ = PhaseTimer.timeTask("sampleRecommendation")) {
            sample = sampler.sample();
        }

        int beatmapid = sample.beatmapId();

        BeatmapMeta loadBeatmap;
        if (sample.mods() < 0) {
            loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
        } else {
            loadBeatmap = backend.loadBeatmap(beatmapid, sample.mods(), lang);
            if (loadBeatmap == null) {
                loadBeatmap = backend.loadBeatmap(beatmapid, 0, lang);
            }
        }
        if (loadBeatmap == null) {
            throw new RareUserException(lang.excuseForError());
        }
        loadBeatmap.setPersonalPP(sample.personalPP());

        /*
         * save recommendation internally
         */

        Recommendation recommendation = new Recommendation(loadBeatmap, sample);

        lastRecommendation.put(userid, recommendation);

        return recommendation;
    }

    public RecommendationRequest parseSamplerSettings(OsuApiUser apiUser, @Nonnull String message, Language lang)
            throws UserException, SQLException, IOException {
        return parser.parseSamplerSettings(apiUser, message, lang);
    }

    private Sampler<BareRecommendation, RecommendationRequest> loadSampler(
            @UserId int userid, RecommendationRequest settings) throws SQLException, IOException, UserException {
        Sampler<BareRecommendation, RecommendationRequest> sampler;
        List<TopPlay> topPlays = recommender.loadTopPlays(userid);
        Set<Integer> exclude = loadGivenRecommendations(userid).stream()
                .map(GivenRecommendation::getBeatmapid)
                .collect(toSet());

        for (TopPlay play : topPlays) {
            LinkedList<Mods> effectiveMods = getEffectiveMods(getMods(play.getMods()));
            effectiveMods.remove(Nightcore); // DT is on when NC is on
            play.setMods(getMask(effectiveMods));

            exclude.add(play.getBeatmapid());
        }

        if (settings.difficultyShift() != Shift.NONE) {
            int limit =
                    switch (settings.difficultyShift()) {
                        case SUCC -> topPlays.size() / 2;
                        case SUCCER -> topPlays.size() / 4;
                        case SUCCERBERG -> 5;
                        default -> throw new IllegalStateException();
                    };
            topPlays = topPlays.stream()
                    .sorted(Comparator.comparingDouble(TopPlay::getPp))
                    .limit(limit)
                    .collect(toList());
        }

        Collection<BareRecommendation> recommendations = recommender.loadRecommendations(
                topPlays, exclude, settings.model(), settings.nomod(), settings.requestedMods());

        // only keep the 1k most probable recommendations to save some
        // memory
        recommendations = getTopRecommendations(recommendations, settings.predicates());

        sampler = new Sampler<>(recommendations, settings, BareRecommendation::probability);
        return sampler;
    }

    private List<BareRecommendation> getTopRecommendations(
            Collection<BareRecommendation> recommendations, List<RecommendationPredicate> predicates)
            throws SQLException, IOException {
        List<BareRecommendation> list = new ArrayList<>();

        recommendationsLoop:
        for (BareRecommendation bareRecommendation : recommendations) {
            OsuApiBeatmap beatmap = null;
            for (RecommendationPredicate predicate : predicates) {
                if (beatmap == null) {
                    beatmap = beatmapsLoader.getBeatmap(bareRecommendation.beatmapId(), 0L);
                }
                if (beatmap == null) {
                    continue recommendationsLoop;
                }
                if (!predicate.test(bareRecommendation, beatmap)) {
                    continue recommendationsLoop;
                }
            }
            list.add(bareRecommendation);
        }

        Collections.sort(
                list,
                Comparator.comparingDouble(BareRecommendation::probability).reversed());

        int size = Math.min(list.size(), 1000);
        ArrayList<BareRecommendation> arrayList = new ArrayList<>(size);
        arrayList.addAll(list.subList(0, size));

        return arrayList;
    }

    /** forgets all given recommendations of the past for a single user */
    public void forgetRecommendations(@UserId int user) throws SQLException {
        try (Database db = dbm.getDatabase();
                PreparedStatement statement =
                        db.prepare("update givenrecommendations set forgotten = true where userid = ?"); ) {
            Loader.setParameters(statement, user);
            statement.executeUpdate();
        }
    }

    public void saveGivenRecommendation(@UserId int userid, @BeatmapId int beatmapid, @BitwiseMods long mods)
            throws SQLException {
        try (var _ = PhaseTimer.timeTask("saveRecommendation")) {
            GivenRecommendation givenRecommendation =
                    new GivenRecommendation(userid, beatmapid, System.currentTimeMillis(), mods);
            dbm.persist(givenRecommendation, Action.INSERT);
        }
    }

    /**
     * recommendations from the last four weeks
     *
     * @param userid
     * @return ordered by date given from newest to oldest
     */
    public List<GivenRecommendation> loadGivenRecommendations(@UserId int userid) throws SQLException {
        try (Database db = dbm.getDatabase();
                Loader<GivenRecommendation> loader = db.loader(
                        GivenRecommendation.class,
                        "where userid = ? and `date` > ? and not forgotten order by `date` desc")) {
            return loader.queryList(userid, System.currentTimeMillis() - 28l * 24 * 60 * 60 * 1000);
        }
    }

    /**
     * Hide a recommendation. It will no longer be displayed in ppaddict, but still taken into account when generating
     * new recommendations.
     */
    public void hideRecommendation(@UserId int userId, @BeatmapId int beatmapid, @BitwiseMods long mods)
            throws SQLException {
        try (Database db = dbm.getDatabase();
                PreparedStatement statement = db.prepare(
                        "update givenrecommendations set hidden = true where userid = ? and beatmapid = ? and mods = ?"); ) {
            Loader.setParameters(statement, userId, beatmapid, mods);
            statement.executeUpdate();
        }
    }

    /**
     * non-hidden recommendations. These might be older than four weeks.
     *
     * @return ordered by date given from newest to oldest
     */
    public List<GivenRecommendation> loadVisibleRecommendations(@UserId int userId) throws SQLException {
        try (Database db = dbm.getDatabase();
                Loader<GivenRecommendation> loader =
                        db.loader(GivenRecommendation.class, "where userid = ? and not hidden order by `date` desc")) {
            return loader.queryList(userId);
        }
    }

    public void forceUpdateTopScores(@UserId int userId) throws SQLException, IOException {
        try (Database db = dbm.getDatabase()) {
            Player.getPlayer(db, userId).updateTop50(db, 0, osuApi, clock, config);
        }
    }
}
