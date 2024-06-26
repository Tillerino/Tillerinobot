package tillerino.tillerinobot.data;

import static java.util.stream.Collectors.toMap;

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

import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Loader;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Table;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;

/**
 * Stores on {@link OsuApiBeatmap} object in the database.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("apibeatmaps")
@KeyColumn({ "beatmapId", "mods" })
@ToString(callSuper=true)
public class ApiBeatmap extends OsuApiBeatmap {
	@MillisSinceEpoch
	public long downloaded = System.currentTimeMillis();

	@BitwiseMods
	public long mods = 0;

	public BeatmapWithMods idAndMods() {
		return new BeatmapWithMods(getBeatmapId(), getMods());
	}

	/**
	 * @param maxAge if > 0, maximum age in milliseconds
	 */
	@CheckForNull
	public static ApiBeatmap loadOrDownload(Database database, @BeatmapId int beatmapid, @BitwiseMods long mods, long maxAge, Downloader downloader) throws SQLException, IOException {
		BeatmapWithMods idAndMods = new BeatmapWithMods(beatmapid, mods);
		return loadOrDownload(database, List.of(idAndMods), maxAge, downloader).get(idAndMods);
	}

	private static ApiBeatmap loadOrDownloadPreloaded(Database database, @BeatmapId int beatmapid, @BitwiseMods long mods, long maxAge,
			Downloader downloader, @CheckForNull ApiBeatmap beatmap) throws IOException, SQLException {
		if(beatmap == null || (maxAge > 0 && beatmap.downloaded < System.currentTimeMillis() - maxAge)) {
			System.out.printf("downloading api beatmap %s/%s (%s; approved %s)%n", beatmapid, mods, beatmap != null ? "outdated" : "new", beatmap != null ? beatmap.getApproved() : "-");
			beatmap = downloader.getBeatmap(beatmapid, mods, ApiBeatmap.class);
			System.out.printf(".downloaded api beatmap %s/%s (%s; approved %s)%n", beatmapid, mods, beatmap != null ? "exists" : "missed", beatmap != null ? beatmap.getApproved() : "-");

			if(beatmap == null) {
				database.delete(ApiBeatmap.class, false, beatmapid, mods);
				return null;
			}
			
			try(Persister<ApiBeatmap> persister = database.persister(ApiBeatmap.class, Action.REPLACE)) {
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
	public static Map<BeatmapWithMods, ApiBeatmap> loadOrDownload(Database database, Collection<BeatmapWithMods> beatmapsWithMods, long maxAge, Downloader downloader) throws SQLException, IOException {
		if (beatmapsWithMods.isEmpty()) {
			return Collections.emptyMap();
		}

		// query from database in single query
		String combinations = beatmapsWithMods.stream().map(bwm -> "(" + bwm.beatmap() + "," + bwm.mods() + ")").collect(Collectors.joining(",", "(", ")"));
		Map<BeatmapWithMods, ApiBeatmap> loaded;
		try (Loader<ApiBeatmap> loader = database.loader(ApiBeatmap.class, "where (`beatmapid`, `mods`) in " + combinations)) {
			loaded = loader.queryList().stream().collect(toMap(ApiBeatmap::idAndMods, Function.identity()));
		}

		// hit rate will be very high
		Map<BeatmapWithMods, ApiBeatmap> allFresh = new LinkedHashMap<>();
		for (BeatmapWithMods idAndMods : beatmapsWithMods) {
			ApiBeatmap fresh = loadOrDownloadPreloaded(database, idAndMods.beatmap(), idAndMods.mods(), maxAge, downloader, loaded.get(idAndMods));
			if (fresh != null) {
				allFresh.put(idAndMods, fresh);
			}
		}
		return allFresh;
	}
}
