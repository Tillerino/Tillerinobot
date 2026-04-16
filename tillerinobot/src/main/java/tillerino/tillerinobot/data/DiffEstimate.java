package tillerino.tillerinobot.data;

import com.github.omkelderman.sandoku.DiffResult;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.tillerino.jagger.annotations.*;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
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
@Table(name = "diffestimates")
@JdbcConfig(quoteChar = "`")
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
    @Id
    @BeatmapId
    public int beatmapid;

    @Id
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

    @Column(name = "maxMaxCombo")
    public int maxCombo;

    public int circleCount;
    public int sliderCount;
    public int spinnerCount;

    public DiffEstimate(@BeatmapId int beatmapid, @BitwiseMods long mods) {
        mods = DiffEstimateProvider.getDiffMods(mods);

        this.beatmapid = beatmapid;
        this.mods = mods;
    }

    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect
        List<DiffEstimate> getAll(Connection c) throws SQLException;

        @JdbcSelect(where = "`beatmapid` = :beatmapid and `mods` = :mods")
        Optional<DiffEstimate> findOne(Connection c, @BeatmapId int beatmapid, @BitwiseMods long mods)
                throws SQLException;

        @JdbcSelect("select * from diffestimates where dataVersion != :version limit 1")
        Optional<DiffEstimate> getOutdated(Connection c, int version) throws SQLException;

        @JdbcSelect(where = "`success`")
        Iterable<DiffEstimate> iterateSuccessful(Connection c) throws SQLException;

        @JdbcInsert
        void insert(Connection c, DiffEstimate diffEstimate) throws SQLException;

        @JdbcInsert("REPLACE INTO `diffestimates` (`diffEstimate.#columns`) VALUES (:diffEstimate.#values)")
        void replace(Connection c, DiffEstimate diffEstimate) throws SQLException;

        @JdbcSelect
        List<DiffEstimate> getMultiple(ResultSet resultSet) throws SQLException;

        @JdbcUpdate("DELETE from diffestimates where (`beatmapid`, `mods`) = (:beatmapid, :mods)")
        void deleteByBeatmapIdAndMods(Connection c, @BeatmapId int beatmapid, @BitwiseMods long mods)
                throws SQLException;

        @JdbcUpdate("DELETE from diffestimates where `beatmapid` = :beatmapid")
        void deleteByBeatmapId(Connection c, @BeatmapId int beatmapid) throws SQLException;
    }
}
