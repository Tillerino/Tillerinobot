package org.tillerino.ppaddict.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.ppaddict.client.services.BeatmapTableService;
import org.tillerino.ppaddict.server.PersistentUserData.Comment;
import org.tillerino.ppaddict.server.PpaddictBackend.BeatmapData;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.Beatmap.Personalization;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest.Sort;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
      @CheckForNull Credentials credentials, PersistentUserData userData) throws PpaddictException {
    System.out.println("server got request: " + request);
    Settings settings = userData != null ? userData.getSettings() : Settings.DEFAULT_SETTINGS;

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
    if (userData != null && request.getSearches().getSearchComment().length() > 0) {
      commentSearchNeedle = request.getSearches().getSearchComment().toLowerCase();
    } else {
      commentSearchNeedle = null;
    }

    boolean useRangeFilters = (textSearchNeedle == null && commentSearchNeedle == null)
        || (settings.isApplyOtherFiltersWithTextFilter());

    Collection<BeatmapData> selection = new ArrayList<>();

    Map<BeatmapWithMods, BeatmapData> beatmaps = backend.getBeatmaps();
    if (beatmaps == null) {
      throw new PpaddictException("The server is restarting or something.");
    }
    beatmaps: for (BeatmapData data : beatmaps.values()) {
      OsuApiBeatmap apiBeatmap = data.getBeatmap();
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
        Comment c = userData.getBeatMapComment(apiBeatmap.getBeatmapId(), estimate.getMods());

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

  public Collection<BeatmapData> sort(final BeatmapRangeRequest request,
      Collection<BeatmapData> selection, Settings settings) {
    if (request.sortBy != null) {
      final ToDoubleFunction<BeatmapData> sortProperty = getComparator(request.sortBy, settings);
      if (sortProperty != null) {
        /*
         * n * log n sorting, but with one-time evaluation of function.
         */
        TreeMap<Double, BeatmapData> sortedBeatmaps = new TreeMap<>();
        for (BeatmapData beatmapData : selection) {
          sortedBeatmaps.put(request.direction * sortProperty.applyAsDouble(beatmapData),
              beatmapData);
        }
        selection = sortedBeatmaps.values();
      }
    }
    return selection;
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

    OsuApiBeatmap apiBeatmap = data.getBeatmap();
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
      final Settings settings) {
    final ToDoubleFunction<BeatmapData> comparator;
    switch (sortBy) {
      case EXPECTED:
        comparator = new ToDoubleFunction<BeatmapData>() {
          @Override
          public double applyAsDouble(BeatmapData value) {
            return value.getEstimates().getPP(settings.getLowAccuracy() / 100);
          }
        };
        break;
      case PERFECT:
        comparator = new ToDoubleFunction<BeatmapData>() {
          @Override
          public double applyAsDouble(BeatmapData value) {
            return value.getEstimates().getPP(settings.getHighAccuracy() / 100);
          }
        };
        break;
      case BPM:
        comparator = new ToDoubleFunction<BeatmapData>() {
          @Override
          public double applyAsDouble(BeatmapData value) {
            return value.getBeatmap().getBpm(value.getEstimates().getMods());
          }
        };
        break;
      case LENGTH:
        comparator = new ToDoubleFunction<BeatmapData>() {
          @Override
          public double applyAsDouble(BeatmapData value) {
            return value.getBeatmap().getTotalLength(value.getEstimates().getMods());
          }
        };
        break;
      case STAR_DIFF:
        comparator = new ToDoubleFunction<BeatmapData>() {
          @SuppressFBWarnings(value = "NP", justification = "null star diff was filtered out")
          @Override
          public double applyAsDouble(BeatmapData value) {
            return value.getEstimates().getStarDiff();
          }
        };
        break;
      default:
        comparator = null;
    }
    return comparator;
  }

  public static Beatmap fromRecord(CSVRecord record) {
    if (record.size() != 12) {
      throw new RuntimeException(
          "unexpected line length: " + record.size() + " in record " + record.getRecordNumber());
    }
    Beatmap b = new Beatmap();
    b.title = record.get("Title");
    b.artist = record.get("Artist");
    b.version = record.get("Version");

    b.beatmapid = Integer.parseInt(record.get("beatmapid"));
    b.setid = Integer.parseInt(record.get("setid"));
    b.approachRate = Integer.parseInt(record.get("ApproachRate"));
    b.circleSize = Integer.parseInt(record.get("CircleSize"));
    b.bpm = Integer.parseInt(record.get("BPM"));
    b.setFormattedLength(record.get("length"));

    String perfectpp = record.get("perfectpp");
    b.highPP = Integer.parseInt(perfectpp);

    b.starDifficulty = Double.valueOf(record.get("StarDifficulty"));
    b.lowPP = Double.valueOf(record.get("expectedpp"));
    return b;
  }

  public Beatmap makeBeatmap(PersistentUserData userData, final BeatmapMeta meta) {
    BeatmapData data = new BeatmapData() {
      @Override
      public PercentageEstimates getEstimates() {
        return meta.getEstimates();
      }

      @Override
      public OsuApiBeatmap getBeatmap() {
        return meta.getBeatmap();
      }
    };
    Beatmap beatmap = makeBeatmap(userData, data);
    return beatmap;
  }

}
