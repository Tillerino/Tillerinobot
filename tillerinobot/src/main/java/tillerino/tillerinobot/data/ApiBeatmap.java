package tillerino.tillerinobot.data;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.CheckForNull;

import org.tillerino.mormon.Database;
import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Persister;
import org.tillerino.mormon.Table;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stores on {@link OsuApiBeatmap} object in the database.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Table("apibeatmaps")
@KeyColumn({ "beatmapId", "mods" })
@ToString(callSuper=true)
public class ApiBeatmap extends OsuApiBeatmap {
	public long downloaded = System.currentTimeMillis();

	public long mods = 0;

	/**
	 * @param maxAge if > 0, maximum age in milliseconds
	 */
	@CheckForNull
	public static ApiBeatmap loadOrDownload(Database database, @BeatmapId int beatmapid, @BitwiseMods long mods, long maxAge, Downloader downloader) throws SQLException, IOException {
		ApiBeatmap beatmap = database.loadUnique(ApiBeatmap.class, beatmapid, mods).orElse(null);
		
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
}
