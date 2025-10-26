package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BeatmapsLoaderImpl implements BeatmapsLoader {
    private final DatabaseManager databaseManager;

    private final OsuApi downloader;

    @Override
    public OsuApiBeatmap getBeatmap(int beatmapId, long mods) throws SQLException, IOException {
        try (Database database = databaseManager.getDatabase()) {
            return ApiBeatmap.loadOrDownload(database, beatmapId, mods, 0, downloader);
        }
    }
}
