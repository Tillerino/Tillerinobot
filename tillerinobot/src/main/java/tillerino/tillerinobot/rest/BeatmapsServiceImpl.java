package tillerino.tillerinobot.rest;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import tillerino.tillerinobot.OsuApi;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiBeatmap.Mapper;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class BeatmapsServiceImpl implements BeatmapsService {
    private static class BeatmapResourceImpl extends AbstractBeatmapResource {
        public BeatmapResourceImpl(
                DatabaseManager dbm, BeatmapDownloader downloader, OsuApiBeatmap beatmap, ActualBeatmap.Repo repo) {
            super(dbm, downloader, beatmap, repo);
        }

        @Override
        public OsuApiBeatmap get() {
            return beatmap;
        }
    }

    private final DatabaseManager databaseManager;

    private final BeatmapDownloader downloader;

    private final OsuApi apiDownloader;

    private final ActualBeatmap.Repo actualBeatmapRepo;

    private final ApiBeatmap.Repo apiBeatmapRepo;

    @Override
    public BeatmapResource byId(int id) {
        try (Database db = databaseManager.getDatabase()) {
            ApiBeatmap beatmap = ApiBeatmap.loadOrDownload(apiBeatmapRepo, db, id, 0L, 0, apiDownloader);
            if (beatmap == null) {
                throw new NotFoundException();
            }

            return new BeatmapResourceImpl(
                    databaseManager, downloader, Mapper.INSTANCE.toApi(beatmap), actualBeatmapRepo);
        } catch (SQLException e) {
            log.error("Error while loading beatmap", e);
            throw new InternalServerErrorException();
        } catch (IOException e) {
            throw RestUtils.getBadGateway(e);
        }
    }

    @Override
    public BeatmapResource byHash(String hash) {
        try (Database db = databaseManager.getDatabase()) {
            ApiBeatmap beatmap = ApiBeatmap.findByFileMd5(apiBeatmapRepo, db, hash);
            if (beatmap == null) {
                throw new NotFoundException();
            }
            return new BeatmapResourceImpl(
                    databaseManager, downloader, Mapper.INSTANCE.toApi(beatmap), actualBeatmapRepo);
        } catch (SQLException e) {
            log.error("Error while loading beatmap", e);
            throw new InternalServerErrorException();
        }
    }
}
