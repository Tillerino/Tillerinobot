package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.CheckForNull;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

public interface BeatmapsLoader {
    /**
     * Retreives beatmap. Implementation hint: this might be called a *lot* when checking recommendation predicates and
     * should probably be cached.
     *
     * @param beatmapId
     * @return null if not found
     */
    @CheckForNull
    OsuApiBeatmap getBeatmap(@BeatmapId int beatmapId, @BitwiseMods long mods) throws SQLException, IOException;
}
