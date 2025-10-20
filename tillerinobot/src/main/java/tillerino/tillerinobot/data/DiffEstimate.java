package tillerino.tillerinobot.data;

import com.github.omkelderman.sandoku.DiffResult;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.mormon.Column;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Table;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.BeatmapImpl;
import tillerino.tillerinobot.diff.DiffEstimateProvider;
import tillerino.tillerinobot.diff.sandoku.SanDoku;

/**
 * In this class, we store the map-specific parameters that are used to calculate pp. The name "estimates" is historic,
 * since we used to estimate these from observed scores. Nowadays, we can just calculate the data directly with
 * {@link SanDoku}.
 *
 * <p>View update status: SELECT count(*), dataVersion FROM `diffestimates` GROUP BY dataVersion or SELECT (select
 * count(*) from diffestimates WHERE dataVersion = 2) / (select count(*) from `diffestimates`) or SELECT d.dataVersion,
 * a.approved, COUNT(*), COUNT(*) / (SELECT COUNT(*) from diffestimates) * 100 AS pct FROM diffestimates d LEFT JOIN
 * apibeatmaps a ON d.beatmapid = a.beatmapId WHERE a.mods = 0 GROUP BY d.dataVersion, a.approved;
 */
@Data
@NoArgsConstructor
@Table("diffestimates")
@KeyColumn({"beatmapid", "mods"})
public class DiffEstimate {
    @Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
    public interface DiffEstimateToBeatmapImplMapper {
        DiffEstimateToBeatmapImplMapper INSTANCE = Mappers.getMapper(DiffEstimateToBeatmapImplMapper.class);

        @Mapping(target = "modsUsed", source = "mods")
        @Mapping(target = "AimDifficultStrainCount", source = "aimDifficultStrainCount")
        @Mapping(target = "AimDifficultSliderCount", source = "aimDifficultSliderCount")
        @Mapping(target = "SpeedDifficultStrainCount", source = "speedDifficultStrainCount")
        @Mapping(target = "OverallDifficulty", source = "overallDifficulty")
        @Mapping(target = "ApproachRate", source = "approachRate")
        @Mapping(target = "FlashlightDifficulty", source = "flashlight")
        @Mapping(target = "MaxCombo", source = "maxCombo")
        @Mapping(target = "AimDifficulty", source = "aim")
        @Mapping(target = "SpeedDifficulty", source = "speed")
        @Mapping(target = "SpeedNoteCount", source = "speedNoteCount")
        @Mapping(target = "SliderFactor", source = "sliderFactor")
        @Mapping(target = "HitCircleCount", source = "circleCount")
        @Mapping(target = "SliderCount", source = "sliderCount")
        @Mapping(target = "SpinnerCount", source = "spinnerCount")
        @Mapping(target = "StarDiff", source = "starDiff")
        BeatmapImpl map(DiffEstimate estimate);

        @Mapping(target = "beatmapid", ignore = true)
        @Mapping(target = "mods", ignore = true)
        @Mapping(target = "success", ignore = true)
        @Mapping(target = "failure", ignore = true)
        @Mapping(target = "calculated", ignore = true)
        @Mapping(target = "dataVersion", ignore = true)
        @Mapping(target = "md5", ignore = true)
        @Mapping(target = "overallDifficulty", source = "diffResult.beatmapProps.overallDifficulty")
        @Mapping(target = "approachRate", source = "diffResult.beatmapProps.approachRate")
        @Mapping(target = "starDiff", source = "diffResult.diffCalcResult.starRating")
        @Mapping(target = "maxCombo", source = "diffResult.diffCalcResult.maxCombo")
        @Mapping(target = "aim", source = "diffResult.diffCalcResult.aimDifficulty")
        @Mapping(target = "speed", source = "diffResult.diffCalcResult.speedDifficulty")
        @Mapping(target = "flashlight", source = "diffResult.diffCalcResult.flashlightDifficulty")
        @Mapping(target = "sliderFactor", source = "diffResult.diffCalcResult.sliderFactor")
        @Mapping(target = "speedNoteCount", source = "diffResult.diffCalcResult.speedNoteCount")
        @Mapping(target = "aimDifficultStrainCount", source = "diffResult.diffCalcResult.aimDifficultStrainCount")
        @Mapping(target = "aimDifficultSliderCount", source = "diffResult.diffCalcResult.aimDifficultSliderCount")
        @Mapping(target = "speedDifficultStrainCount", source = "diffResult.diffCalcResult.speedDifficultStrainCount")
        @Mapping(target = "circleCount", source = "diffResult.diffCalcResult.hitCircleCount")
        @Mapping(target = "sliderCount", source = "diffResult.diffCalcResult.sliderCount")
        @Mapping(target = "spinnerCount", source = "diffResult.diffCalcResult.spinnerCount")
        void map(DiffResult diffResult, @MappingTarget DiffEstimate database);

        default BeatmapImpl toBeatmap(DiffResult diffResult) {
            DiffEstimate estimate = new DiffEstimate();
            map(diffResult, estimate);
            return map(estimate);
        }
    }

    // all fields are public because Mapstruct <> lombok is broken
    // meta data
    @BeatmapId
    public int beatmapid;

    @BitwiseMods
    public long mods;

    public boolean success = false;
    public String failure;
    public long calculated = System.currentTimeMillis();
    public int dataVersion = SanDoku.VERSION;
    public String md5 = null;

    // calculated parameters
    public double aim;
    public double speed;
    public double starDiff;
    public double flashlight;

    public double sliderFactor;

    public double speedNoteCount;

    public double aimDifficultStrainCount;
    public double aimDifficultSliderCount;
    public double speedDifficultStrainCount;

    public double approachRate;
    public double overallDifficulty;

    @Column("maxMaxCombo")
    public int maxCombo;

    public int circleCount;
    public int sliderCount;
    public int spinnerCount;

    public DiffEstimate(@BeatmapId int beatmapid, @BitwiseMods long mods) {
        mods = DiffEstimateProvider.getDiffMods(mods);

        this.beatmapid = beatmapid;
        this.mods = mods;
    }

    /**
     * Load multiple diff estimates at once.
     *
     * @param beatmaps performance penalty if not unique, but will not throw up
     * @return entries for all diff estimates that could be found. Will not throw if some are missing.
     */
    public static Map<BeatmapWithMods, DiffEstimate> loadMultiple(
            Database database, Collection<BeatmapWithMods> beatmaps) throws SQLException {
        if (beatmaps.isEmpty()) {
            return Collections.emptyMap();
        }
        String combinations = beatmaps.stream()
                .map(bwm -> "(" + bwm.beatmap() + "," + bwm.mods() + ")")
                .collect(Collectors.joining(",", "(", ")"));
        try (Loader<DiffEstimate> loader =
                database.loader(DiffEstimate.class, "where (beatmapid, mods) in " + combinations)) {
            return loader.queryList().stream()
                    .collect(Collectors.toMap(e -> new BeatmapWithMods(e.beatmapid, e.mods), e -> e));
        }
    }
}
