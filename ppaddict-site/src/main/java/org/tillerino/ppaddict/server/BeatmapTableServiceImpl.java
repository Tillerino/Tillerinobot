package org.tillerino.ppaddict.server;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.ppaddict.client.services.BeatmapTableService;
import org.tillerino.ppaddict.server.PersistentUserData.Comment;
import org.tillerino.ppaddict.server.PersistentUserData.Comments;
import org.tillerino.ppaddict.server.PpaddictBackend.BeatmapData;
import org.tillerino.ppaddict.server.PpaddictBackend.OsuApiBeatmapForPpaddict;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.Beatmap.Personalization;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapFilter;
import org.tillerino.ppaddict.shared.BeatmapFilterSettings;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest.Sort;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import lombok.Value;
import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

/**
 * The server-side implementation of the RPC service.
 */
@Singleton
public class BeatmapTableServiceImpl extends RemoteServiceServlet implements BeatmapTableService {
  private static final long serialVersionUID = 1L;

  Logger log = LoggerFactory.getLogger(BeatmapTableServiceImpl.class);

  @Inject
  UserDataServiceImpl userDataService;

  @Inject
  PpaddictBackend backend;

  @Inject
  BotBackend botBackend;

  @Inject
  RecommendationsManager recommendationsManager;

  private final Cache<BeatmapsCacheKey, List<BeatmapData>> requestCache = CacheBuilder.newBuilder()
      .softValues()
      .maximumSize(100)
      .build();

  @Override
  public BeatmapBundle getRange(final BeatmapRangeRequest request) throws PpaddictException {
    Credentials credentials = userDataService.getCredentials(getThreadLocalRequest());

    PersistentUserData userData =
        credentials != null ? userDataService.getServerUserData(credentials) : null;

    BeatmapBundle bundle = executeGetRange(request, credentials, userData);
    bundle.loggedIn = userData != null;

    return bundle;
  }

  public BeatmapBundle executeGetRange(@Nonnull final BeatmapRangeRequest request,
      @CheckForNull Credentials credentials, @CheckForNull PersistentUserData userData) throws PpaddictException {
    BeatmapFilterSettings settings = new BeatmapFilterSettings(userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS);
    Comments comments = userData != null ? userData.getComments() : null;

    List<BeatmapData> selection = getAll(new BeatmapFilter(request), settings, comments);

    if (request.loadedUserRequest && request.getSearches().getBeatmapId() == null
        && request.getSearches().getSetId() == null) {
      if (userData != null) {
        System.out.println("persisting " + request);
        userData.setLastRequest(request);
        userDataService.saveUserData(credentials, userData);
      }
    }

    BeatmapBundle beatmapBundle = makeBundle(request, userData, selection);

    System.out.println(beatmapBundle.beatmaps.size());

    return beatmapBundle;
  }

  private List<BeatmapData> getAll(BeatmapFilter request, BeatmapFilterSettings settings,
      Comments comments) throws PpaddictException {
    requestCache.cleanUp();
    BeatmapsCacheKey key = new BeatmapsCacheKey(request, settings, comments);
    List<BeatmapData> cached = requestCache.getIfPresent(key);
    if (cached != null) {
      return cached;
    }
    System.out.println("running beatmap search for: " + key);
    List<BeatmapData> fresh = executeBeatmapSearch(request, settings, comments);
    requestCache.put(key, fresh);
    return fresh;
  }

  private List<BeatmapData> executeBeatmapSearch(BeatmapFilter request,
      BeatmapFilterSettings settings, Comments comments) throws PpaddictException {
    Collection<Predicate<BeatmapData>> predicates = new ArrayList<>();
    if (request.getSearches().getBeatmapId() != null) {
      int beatmapId = request.getSearches().getBeatmapId();
      predicates.add(x -> x.getBeatmap().getBeatmapId() == beatmapId);
    }
    if (request.getSearches().getSetId() != null) {
      int setId = request.getSearches().getSetId();
      predicates.add(x -> x.getBeatmap().getSetId() == setId);
    }

    /*
     * prepare search objects
     */
    String textSearchNeedle;
    if (request.getSearches().getSearchText().length() > 0) {
      textSearchNeedle = request.getSearches().getSearchText().trim().toLowerCase();
    } else {
      textSearchNeedle = null;
    }
    String commentSearchNeedle;
    if (comments != null && request.getSearches().getSearchComment().length() > 0) {
      commentSearchNeedle = request.getSearches().getSearchComment().toLowerCase();
    } else {
      commentSearchNeedle = null;
    }

    boolean useRangeFilters = (textSearchNeedle == null && commentSearchNeedle == null)
        || (settings.isApplyOtherFiltersWithTextFilter());

    List<BeatmapData> selection = new ArrayList<>();

    Map<BeatmapWithMods, BeatmapData> beatmaps = backend.getBeatmaps();
    if (beatmaps == null) {
      throw new PpaddictException("The server is restarting or something.");
    }
    beatmaps: for (BeatmapData data : beatmaps.values()) {
      OsuApiBeatmapForPpaddict apiBeatmap = data.getBeatmap();
      PercentageEstimates estimate = data.getEstimates();

      for (Predicate<BeatmapData> predicate : predicates) {
        if (!predicate.test(data)) {
          continue beatmaps;
        }
      }
      /*
       * search in name
       */
      if (textSearchNeedle != null) {
        String longTitle = apiBeatmap.getArtist() + " - " + apiBeatmap.getTitle() + " ["
            + apiBeatmap.getVersion() + "]";
        if (!longTitle.toLowerCase().contains(textSearchNeedle)) {
          continue;
        }
      }

      /*
       * search in comment
       */
      if (commentSearchNeedle != null) {
        Comment c = comments.getComment(apiBeatmap.getBeatmapId(), estimate.getMods());

        if (c == null) {
          continue;
        }

        if (commentSearchNeedle.equals("*")) {
          // code for "any comment"
        } else {
          if (!c.text.toLowerCase().contains(commentSearchNeedle)) {
            continue;
          }
        }
      }

      if (useRangeFilters) {
        if (request.aR.min != null
            && apiBeatmap.getApproachRate(estimate.getMods()) * 100 < request.aR.min) {
          continue;
        }
        if (request.aR.max != null
            && apiBeatmap.getApproachRate(estimate.getMods()) * 100 > request.aR.max) {
          continue;
        }
        if (request.oD.min != null
            && apiBeatmap.getOverallDifficulty(estimate.getMods()) * 100 < request.oD.min) {
          continue;
        }
        if (request.oD.max != null
            && apiBeatmap.getOverallDifficulty(estimate.getMods()) * 100 > request.oD.max) {
          continue;
        }
        if (request.cS.min != null
            && apiBeatmap.getCircleSize(estimate.getMods()) * 100 < request.cS.min) {
          continue;
        }
        if (request.cS.max != null
            && apiBeatmap.getCircleSize(estimate.getMods()) * 100 > request.cS.max) {
          continue;
        }
        if (request.perfectPP.min != null
            && (estimate.getPP(settings.getHighAccuracy() / 100d) < request.perfectPP.min)) {
          continue;
        }
        if (request.perfectPP.max != null
            && (estimate.getPP(settings.getHighAccuracy() / 100d) > request.perfectPP.max)) {
          continue;
        }
        if (request.expectedPP.min != null
            && estimate.getPP(settings.getLowAccuracy() / 100d) < request.expectedPP.min) {
          continue;
        }
        if (request.expectedPP.max != null
            && estimate.getPP(settings.getLowAccuracy() / 100d) > request.expectedPP.max) {
          continue;
        }
        if (request.bpm.min != null && apiBeatmap.getBpm(estimate.getMods()) < request.bpm.min) {
          continue;
        }
        if (request.bpm.max != null && apiBeatmap.getBpm(estimate.getMods()) > request.bpm.max) {
          continue;
        }
        if (request.mapLength.min != null
            && apiBeatmap.getTotalLength(estimate.getMods()) < request.mapLength.min) {
          continue;
        }
        if (request.mapLength.max != null
            && apiBeatmap.getTotalLength(estimate.getMods()) > request.mapLength.max) {
          continue;
        }
        Double starDiff = estimate.getStarDiff();
        if (starDiff == null && request.sortBy == Sort.STAR_DIFF) {
          continue;
        }
        // these are multiplied by 100 for fake decimals
        if (request.starDiff.min != null
            && (starDiff == null || starDiff * 100 < request.starDiff.min)) {
          continue;
        }
        if (request.starDiff.max != null
            && (starDiff == null || starDiff * 100 > request.starDiff.max)) {
          continue;
        }
      }

      selection.add(data);
    }

    selection = sort(request, selection, settings);
    return selection;
  }

  public List<BeatmapData> sort(final BeatmapFilter request,
      List<BeatmapData> selection, BeatmapFilterSettings settings) {
    if (request.sortBy != null) {
      final ToDoubleFunction<BeatmapData> sortProperty = getComparator(request.sortBy, settings);
      if (sortProperty != null) {
        ToDoubleFunction<? super BeatmapData> property =
            beatmapData -> request.direction * sortProperty.applyAsDouble(beatmapData);
        return sortedView(selection, property);
      }
    }
    return selection;
  }

  /**
   * Returns a sorted view of a list.
   *
   * @param not modified. Modifications of this object will affect the returned value.
   * @param property the property to sort by. This is only evaluated once per item.
   */
  static List<BeatmapData> sortedView(List<BeatmapData> selection,
      ToDoubleFunction<? super BeatmapData> property) {
    // can't sort a primitive array with a comparator :(
    Integer[] positions = IntStream.range(0, selection.size()).boxed().toArray(Integer[]::new);
    double[] values = selection.stream().mapToDouble(property).toArray();

    Arrays.sort(positions, Comparator.comparingDouble(i -> values[i]));

    return new AbstractList<PpaddictBackend.BeatmapData>() {
      @Override
      public BeatmapData get(int index) {
        return selection.get(positions[index]);
      }

      @Override
      public int size() {
        return positions.length;
      }
    };
  }

  public BeatmapBundle makeBundle(final BeatmapRangeRequest request, PersistentUserData userData,
      Collection<BeatmapData> selection) {
    BeatmapBundle beatmapBundle = new BeatmapBundle();
    beatmapBundle.available = selection.size();
    int x = 0;
    for (BeatmapData data1 : selection) {
      if (x >= Math.min(selection.size(), request.start)) {
        Beatmap beatmap = makeBeatmap(userData, data1);

        beatmapBundle.beatmaps.add(beatmap);
      }
      if (beatmapBundle.beatmaps.size() >= request.length) {
        break;
      }
      x++;
    }
    return beatmapBundle;
  }

  public Beatmap makeBeatmap(PersistentUserData userData, BeatmapData data) {
    Settings settings = userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS;

    OsuApiBeatmapForPpaddict apiBeatmap = data.getBeatmap();
    PercentageEstimates estimates = data.getEstimates();
    long mods = estimates.getMods();

    Beatmap beatmap = new Beatmap();
    beatmap.approachRate = apiBeatmap.getApproachRate(mods);
    beatmap.artist = apiBeatmap.getArtist();
    beatmap.beatmapid = apiBeatmap.getBeatmapId();
    beatmap.bpm = apiBeatmap.getBpm(mods);
    beatmap.circleSize = apiBeatmap.getCircleSize(mods);
    beatmap.lowPP = estimates.getPP(settings.getLowAccuracy() / 100);
    beatmap.length = apiBeatmap.getTotalLength(mods);
    beatmap.highPP = estimates.getPP(settings.getHighAccuracy() / 100);
    if (userData != null) {
      Comment comment = userData.getBeatMapComment(beatmap.beatmapid, mods);
      if (comment != null) {
        Personalization pers = new Personalization();
        pers.comment = comment.text;
        pers.commentDate = BeatmapTableServiceImpl.ago(comment.date);
        beatmap.personalization = pers;
      }
    }
    beatmap.overallDiff = apiBeatmap.getOverallDifficulty(mods);
    beatmap.setid = apiBeatmap.getSetId();
    beatmap.starDifficulty =
        mods == 0 ? (Double) data.getBeatmap().getStarDifficulty() : estimates.getStarDiff();
    beatmap.title = apiBeatmap.getTitle();
    beatmap.version = apiBeatmap.getVersion();

    if (estimates.getMods() != 0) {
      if (estimates.getMods() == -1) {
        beatmap.mods = "?";
      } else {
        beatmap.mods = Mods.toShortNamesContinuous(Mods.getMods(estimates.getMods()));
      }
    }
    return beatmap;
  }

  public static String ago(long time) {
    long diff = System.currentTimeMillis() - time;

    TreeMap<Long, String> times = new TreeMap<>(new Comparator<Long>() {
      @Override
      public int compare(Long o1, Long o2) {
        return o2.compareTo(o1);
      }
    });

    times.put(1000l, "second");
    times.put(60l * 1000l, "minute");
    times.put(60l * 60l * 1000l, "hour");
    times.put(24l * 60l * 60l * 1000l, "day");
    times.put(7l * 24l * 60l * 60l * 1000l, "week");
    times.put(30l * 24l * 60l * 60l * 1000l, "month");
    times.put(365l * 24l * 60l * 60l * 1000l, "year");

    for (Entry<Long, String> entry : times.entrySet()) {
      if (diff > entry.getKey()) {
        long value = diff / entry.getKey();

        String s;

        if (value == 1l) {
          switch (entry.getValue()) {
            case "hour":
              s = "an";
              break;
            default:
              s = "a";
              break;
          }
        } else {
          s = value + "";
        }
        s += " " + entry.getValue();

        s += value > 1 ? "s" : "";

        s += " ago";

        return s;
      }
    }

    return "a moment ago";
  }

  private static ToDoubleFunction<BeatmapData> getComparator(final Sort sortBy,
      final BeatmapFilterSettings settings) {
    switch (sortBy) {
      case EXPECTED:
        return value -> value.getEstimates().getPP(settings.getLowAccuracy() / 100);
      case PERFECT:
        return value -> value.getEstimates().getPP(settings.getHighAccuracy() / 100);
      case BPM:
        return value -> value.getBeatmap().getBpm(value.getEstimates().getMods());
      case LENGTH:
        return value -> value.getBeatmap().getTotalLength(value.getEstimates().getMods());
      case STAR_DIFF:
        return value -> value.getEstimates().getStarDiff();
      default:
        return null;
    }
  }

  public Beatmap makeBeatmap(PersistentUserData userData, final BeatmapMeta meta) {
    BeatmapData data = new BeatmapData(meta.getEstimates(),
        OsuApiBeatmapForPpaddict.Mapper.INSTANCE.shrink(meta.getBeatmap()));
    return makeBeatmap(userData, data);
  }

  @Value
  private static class BeatmapsCacheKey {
    BeatmapFilter request;
    BeatmapFilterSettings settings;
    Comments comments;
  }
}
