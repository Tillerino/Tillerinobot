package tillerino.tillerinobot.data;
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

import lombok.Data;
import lombok.NoArgsConstructor;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.diff.BeatmapImpl;
import tillerino.tillerinobot.diff.sandoku.SanDoku;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In this class, we store the map-specific parameters that are used to calculate pp.
 * The name "estimates" is historic, since we used to estimate these from observed scores.
 * Nowadays, we can just calculate the data directly with {@link SanDoku}.
 * 
 * <p>View update status:
 * SELECT count(*), dataVersion FROM `diffestimates` GROUP BY dataVersion
 * or
 * SELECT (select count(*) from diffestimates WHERE dataVersion = 2) / (select count(*) from `diffestimates`)
 * or
 * SELECT d.dataVersion, a.approved, COUNT(*), COUNT(*) / (SELECT COUNT(*) from diffestimates) * 100 AS pct FROM diffestimates d LEFT JOIN apibeatmaps a ON d.beatmapid = a.beatmapId WHERE a.mods = 0 GROUP BY d.dataVersion, a.approved;
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
		@Mapping(target = "aimDifficultyStrainCount", ignore = true)
		@Mapping(target = "speedDifficultyStrainCount", ignore = true)
		BeatmapImpl map(DiffEstimate estimate);

		@Mapping(target = "beatmapid", ignore = true)
		@Mapping(target = "mods", ignore = true)
		@Mapping(target = "success", ignore = true)
		@Mapping(target = "failure", ignore = true)
		@Mapping(target = "calculated", ignore = true)
		@Mapping(target = "dataVersion", ignore = true)
		@Mapping(target = "md5", ignore = true)
		void map(BeatmapImpl beatmap, @MappingTarget DiffEstimate database);
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

	public double approachRate;
	public double overallDifficulty;

	@Column("maxMaxCombo")
	public int maxCombo;

	public int circleCount;
	public int sliderCount;
	public int spinnerCount;

	public DiffEstimate(@BeatmapId int beatmapid, @BitwiseMods long mods) {
		mods = tillerino.tillerinobot.diff.Beatmap.getDiffMods(mods);

		this.beatmapid = beatmapid;
		this.mods = mods;
	}

	/**
	 * Load multiple diff estimates at once.
	 * @param beatmaps performance penalty if not unique, but will not throw up
	 * @return entries for all diff estimates that could be found. Will not throw if some are missing.
	 */
	public static Map<BeatmapWithMods, DiffEstimate> loadMultiple(Database database, Collection<BeatmapWithMods> beatmaps) throws SQLException {
		if (beatmaps.isEmpty()) {
			return Collections.emptyMap();
		}
		String combinations = beatmaps.stream().map(bwm -> "(" + bwm.beatmap() + "," + bwm.mods() + ")").collect(Collectors.joining(",", "(", ")"));
		try (Loader<DiffEstimate> loader = database.loader(DiffEstimate.class, "where (beatmapid, mods) in " + combinations)) {
			return loader.queryList().stream().collect(Collectors.toMap(e -> new BeatmapWithMods(e.beatmapid, e.mods), e -> e));
		}
	}
}
