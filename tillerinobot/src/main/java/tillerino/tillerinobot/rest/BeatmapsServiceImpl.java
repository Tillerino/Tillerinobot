package tillerino.tillerinobot.rest;


import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;

import org.tillerino.mormon.Database;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Loader;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import com.google.inject.AbstractModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.rest.AbstractBeatmapResource.BeatmapDownloader;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class BeatmapsServiceImpl implements BeatmapsService {
	private class BeatmapResourceImpl extends AbstractBeatmapResource {
		public BeatmapResourceImpl(DatabaseManager dbm, BeatmapDownloader downloader, OsuApiBeatmap beatmap) {
			super(dbm, downloader, beatmap);
		}

		@Override
		public OsuApiBeatmap get() {
			return beatmap;
		}
	}

	public static class Module extends AbstractModule {
		@Override
		protected void configure() {
			bind(BeatmapsService.class).to(BeatmapsServiceImpl.class);
		}
	}

	private final DatabaseManager databaseManager;

	private final BeatmapDownloader downloader;

	private final Downloader apiDownloader;

	@Override
	public BeatmapResource byId(int id) {
		try (Database database = databaseManager.getDatabase()) {
			ApiBeatmap beatmap = ApiBeatmap.loadOrDownload(database, id, 0L, 0, apiDownloader);
			if (beatmap == null) {
				throw new NotFoundException();
			}

			return new BeatmapResourceImpl(databaseManager, downloader, beatmap);
		} catch (SQLException e) {
			log.error("Error while loading beatmap", e);
			throw new InternalServerErrorException();
		} catch (IOException e) {
			throw RestUtils.getBadGateway(e);
		}
	}

	@Override
	public BeatmapResource byHash(String hash) {
		try (Database database = databaseManager.getDatabase();
				Loader<ApiBeatmap> loader = database.loader(ApiBeatmap.class, "where `fileMd5` = ?")) {
			return new BeatmapResourceImpl(databaseManager, downloader, loader.queryUnique(hash).orElseThrow(NotFoundException::new));
		} catch (SQLException e) {
			log.error("Error while loading beatmap", e);
			throw new InternalServerErrorException();
		}
	}

}
