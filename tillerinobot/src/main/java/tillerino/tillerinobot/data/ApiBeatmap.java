package tillerino.tillerinobot.data;

import static java.util.stream.Collectors.toMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.*;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;

/** Stores on {@link OsuApiBeatmap} object in the database. */
@Data
@EqualsAndHashCode
@Table("apibeatmaps")
@KeyColumn({"beatmapId", "mods"})
@ToString(callSuper = true)
public class ApiBeatmap {
    @MillisSinceEpoch
    public long downloaded = System.currentTimeMillis();

    @BitwiseMods
    public long mods = 0;

    @BeatmapId
    private int beatmapId;

    @BeatmapSetId
    private int setId;

    private String artist;
    private String title;
    private String version;
    private String creator;
    private String source;
    private String tags;

    private int creatorId;

    private int genreId;

    private int languageId;

    /**
     *
     *
     * <ul>
     *   <li>{@value OsuApiBeatmap#GRAVEYARD} = {@link OsuApiBeatmap#GRAVEYARD}
     *   <li>{@value OsuApiBeatmap#WIP} = {@link OsuApiBeatmap#WIP}
     *   <li>{@value OsuApiBeatmap#PENDING} = {@link OsuApiBeatmap#PENDING}
     *   <li>{@value OsuApiBeatmap#RANKED} = {@link OsuApiBeatmap#RANKED}
     *   <li>{@value OsuApiBeatmap#APPROVED} = {@link OsuApiBeatmap#APPROVED}
     *   <li>{@value OsuApiBeatmap#QUALIFIED} = {@link OsuApiBeatmap#QUALIFIED}
     *   <li>{@value OsuApiBeatmap#LOVED} = {@link OsuApiBeatmap#LOVED}
     * </ul>
     */
    private int approved;

    /** may be null if not ranked */
    @MillisSinceEpoch
    private Long approvedDate;

    @MillisSinceEpoch
    private long lastUpdate;

    private double bpm; // can this be non-integral?

    /** Star difficulty */
    private double starDifficulty;

    private double aimDifficulty;

    private double speedDifficulty;

    /** Overall difficulty (OD) */
    private double overallDifficulty;

    /** Circle size value (CS) */
    private double circleSize;

    /** Approach Rate (AR) */
    private double approachRate;

    /** Healthdrain (HP) */
    private double healthDrain;

    /** seconds from first note to last note not including breaks */
    private int hitLength;

    /** seconds from first note to last note including breaks */
    private int totalLength;

    /** mode (0 = osu!, 1 = Taiko, 2 = CtB, 3 = osu!mania) */
    @GameMode
    private int mode;

    /** md5 hash of the beatmap */
    private String fileMd5;

    /** Number of times the beatmap was favourited. (americans: notice the ou!) */
    private int favouriteCount;

    /** Number of times the beatmap was played */
    private int playCount;

    /** Number of times the beatmap was passed, completed (the user didn't fail or retry) */
    private int passCount;

    /** The maximum combo an user can reach playing this beatmap. */
    private int maxCombo;

    public double getApproachRate(@BitwiseMods long mods) {
        return OsuApiBeatmap.calcAR(this.getApproachRate(), mods);
    }

    public double getOverallDifficulty(@BitwiseMods long mods) {
        return OsuApiBeatmap.calcOd(this.getOverallDifficulty(), mods);
    }

    public double getBpm(@BitwiseMods long mods) {
        return OsuApiBeatmap.calcBpm(this.getBpm(), mods);
    }

    public int getTotalLength(@BitwiseMods long mods) {
        return OsuApiBeatmap.calcTotalLength(this.getTotalLength(), mods);
    }

    public double getCircleSize(@BitwiseMods long mods) {
        return OsuApiBeatmap.calcCircleSize(this.getCircleSize(), mods);
    }

    public BeatmapWithMods idAndMods() {
        return new BeatmapWithMods(getBeatmapId(), getMods());
    }

    /** @param maxAge if > 0, maximum age in milliseconds */
    @CheckForNull
    public static ApiBeatmap loadOrDownload(
            Database database, @BeatmapId int beatmapid, @BitwiseMods long mods, long maxAge, OsuApi downloader)
            throws SQLException, IOException {
        BeatmapWithMods idAndMods = new BeatmapWithMods(beatmapid, mods);
        return loadOrDownload(database, List.of(idAndMods), maxAge, downloader).get(idAndMods);
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    private static ApiBeatmap loadOrDownloadPreloaded(
            Database database,
            @BeatmapId int beatmapid,
            @BitwiseMods long mods,
            long maxAge,
            OsuApi downloader,
            @CheckForNull ApiBeatmap beatmap)
            throws IOException, SQLException {
        if (beatmap == null || (maxAge > 0 && beatmap.downloaded < System.currentTimeMillis() - maxAge)) {
            try (var _ = PhaseTimer.timeTask("downloadBeatmap")) {
                System.out.printf(
                        "downloading api beatmap %s/%s (%s; approved %s)%n",
                        beatmapid,
                        mods,
                        beatmap != null ? "outdated" : "new",
                        beatmap != null ? beatmap.getApproved() : "-");
                beatmap = Mapper.INSTANCE.fromApi(
                        downloader.getBeatmap(beatmapid, mods), mods, System.currentTimeMillis());
                System.out.printf(
                        ".downloaded api beatmap %s/%s (%s; approved %s)%n",
                        beatmapid,
                        mods,
                        beatmap != null ? "exists" : "missed",
                        beatmap != null ? beatmap.getApproved() : "-");
            }

            if (beatmap == null) {
                var _ = database.deleteFrom(ApiBeatmap.class)
                        .execute("where `beatmapId` = ", beatmapid, " and `mods` = ", mods);
                return null;
            }

            try (var _ = PhaseTimer.timeTask("persistBeatmap");
                    Persister<ApiBeatmap> persister = database.persister(ApiBeatmap.class, Action.REPLACE)) {
                beatmap.setMods(mods);
                persister.persist(beatmap);
            }
        }

        return beatmap;
    }

    /**
     * @param beatmapsWithMods care that this is unique
     * @return might not contain entries for all requests
     */
    public static Map<BeatmapWithMods, ApiBeatmap> loadOrDownload(
            Database database, Collection<BeatmapWithMods> beatmapsWithMods, long maxAge, OsuApi downloader)
            throws SQLException, IOException {
        if (beatmapsWithMods.isEmpty()) {
            return Collections.emptyMap();
        }

        // query from database in single query
        String combinations = beatmapsWithMods.stream()
                .map(bwm -> "(" + bwm.beatmap() + "," + bwm.mods() + ")")
                .collect(Collectors.joining(",", "(", ")"));
        Map<BeatmapWithMods, ApiBeatmap> loaded;
        try (var _ = PhaseTimer.timeTask("loadBeatmaps");
                Loader<ApiBeatmap> loader =
                        database.loader(ApiBeatmap.class, "where (`beatmapid`, `mods`) in " + combinations)) {
            loaded = loader.queryList().stream().collect(toMap(ApiBeatmap::idAndMods, Function.identity()));
        }

        // hit rate will be very high
        Map<BeatmapWithMods, ApiBeatmap> allFresh = new LinkedHashMap<>();
        for (BeatmapWithMods idAndMods : beatmapsWithMods) {
            ApiBeatmap fresh = loadOrDownloadPreloaded(
                    database, idAndMods.beatmap(), idAndMods.mods(), maxAge, downloader, loaded.get(idAndMods));
            if (fresh != null) {
                allFresh.put(idAndMods, fresh);
            }
        }
        return allFresh;
    }

    @org.mapstruct.Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface Mapper {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);

        @Mapping(target = "mods", source = "mods")
        @Mapping(target = "downloaded", source = "downloaded")
        ApiBeatmap fromApi(OsuApiBeatmap api, @BitwiseMods long mods, long downloaded);

        OsuApiBeatmap toApi(ApiBeatmap api);
    }
}
