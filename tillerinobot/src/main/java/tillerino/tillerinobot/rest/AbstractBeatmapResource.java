package tillerino.tillerinobot.rest;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.tillerino.mormon.DatabaseManager;
import org.tillerino.mormon.Persister.Action;
import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.ppaddict.util.MdcUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.data.ActualBeatmap;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBeatmapResource implements BeatmapResource {
	public interface BeatmapDownloader {
		@GET
		@Path("/osu/{beatmapid}")
		@Produces(MediaType.TEXT_PLAIN)
		String getActualBeatmap(@PathParam("beatmapid") int beatmapid);

		/**
		 * Creates a test implementation that loads beatmaps from the classpath.
		 *
		 * @param cls A class from the class loader that can load the resources. If you
		 *            are creating this in a unit test where the resources are in the
		 *            same module, getClass() will probably work just fine.
		 * @return an implementation of {@link BeatmapDownloader} which will load
		 *         beatmaps from the class path. It will load /beatmaps/|beatmapid|.osu
		 *         as UTF-8 and throw JAX-RS exceptions on errors.
		 */
		public static BeatmapDownloader createTestLoader(Class<?> cls) {
			return beatmapid -> {
				try (InputStream is = cls.getResourceAsStream("/beatmaps/" + beatmapid + ".osu")) {
					if (is == null) {
						throw new NotFoundException("beatmap " + beatmapid);
					}
					return IOUtils.toString(is, StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new InternalServerErrorException(e);
				}
			};
		}
	}

	protected final DatabaseManager dbm;

	protected final BeatmapDownloader downloader;

	protected final OsuApiBeatmap beatmap;

	@Override
	public String getFile() {
		try {
			ActualBeatmap found = dbm.selectUnique(ActualBeatmap.class).execute("where beatmapid = ", beatmap.getBeatmapId()).orElse(null);
			if (found != null) {
				if (found.getHash() == null || found.getHash().isEmpty()) {
					found.setHash(md5Hex(found.decompressedContent()));
					dbm.persist(found, Action.REPLACE);
				}
				byte[] content = found.getContent();
				if (content != null) {
					found.compressContent(content);
					dbm.persist(found, Action.REPLACE);
				}
			}
			if (found == null || (!found.getHash().equals(beatmap.getFileMd5())
					&& (found.getDownloaded() < beatmap.getLastUpdate()
							// at most once per hour in case there is a problem
							|| found.getDownloaded() < System.currentTimeMillis() - HOURS.toMillis(1)))) {
				MdcUtils.incrementCounter(MdcUtils.MDC_EXTERNAL_API_CALLS);
				String downloaded = downloader.getActualBeatmap(beatmap.getBeatmapId());
				if (found == null) {
					found = new ActualBeatmap();
					found.setBeatmapid(beatmap.getBeatmapId());
				}
				found.compressContent(downloaded.getBytes(UTF_8));
				found.setDownloaded(System.currentTimeMillis());
				found.setHash(md5Hex(downloaded));
				dbm.persist(found, Action.REPLACE);
			}
			if (!found.getHash().equals(beatmap.getFileMd5())) {
				throw new WebApplicationException(Response.status(Status.BAD_GATEWAY)
						.entity(format("Beatmap %s is damaged. Expected hash code: %s Actual: %s", beatmap.getBeatmapId(),
								beatmap.getFileMd5(), found.getHash()))
						.build());
			}
			return new String(found.decompressedContent(), UTF_8);
		} catch (SQLException e) {
			log.error("database error", e);
			throw new InternalServerErrorException();
		}
	}

	@Override
	public void setFile(String content) {
		try {
			String hash = DigestUtils.md5Hex(content);
			if (!hash.equals(beatmap.getFileMd5())) {
				throw new WebApplicationException(
						String.format("Hash does not match. Expected: %s Actual: %s", beatmap.getFileMd5(), hash),
						Status.FORBIDDEN);
			}
			ActualBeatmap found = dbm.selectUnique(ActualBeatmap.class).execute("where beatmapid = ", beatmap.getBeatmapId()).orElse(null);
			if (found == null) {
				found = new ActualBeatmap();
				found.setBeatmapid(beatmap.getBeatmapId());
			}
			found.compressContent(content.getBytes(UTF_8));
			found.setDownloaded(System.currentTimeMillis());
			found.setHash(hash);
			dbm.persist(found, Action.REPLACE);
		} catch (SQLException e) {
			log.error("database error", e);
			throw new InternalServerErrorException();
		}
	}
}
