package tillerino.tillerinobot;

import java.io.IOException;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import tillerino.tillerinobot.data.ApiBeatmap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BeatmapsLoaderImpl implements BeatmapsLoader {
    private final DatabaseManager databaseManager;

    private final OsuApi downloader;

    private final ApiBeatmap.Repo repo;

    @Override
    public ApiBeatmap getBeatmap(int beatmapId, long mods) throws SQLException, IOException {
        try (Database db = databaseManager.getDatabase()) {
            return ApiBeatmap.loadOrDownload(repo, db.connection(), beatmapId, mods, 0, downloader);
        }
    }
}
