package tillerino.tillerinobot.rest;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.tillerino.osuApiModel.OsuApiBeatmap;

import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.data.ActualBeatmap;
import tillerino.tillerinobot.data.repos.ActualBeatmapRepository;

@RequiredArgsConstructor
public abstract class AbstractBeatmapResource implements BeatmapResource {
	public interface BeatmapDownloader {
		@GET
		@Path("/osu/{beatmapid}")
		@Produces(MediaType.TEXT_PLAIN)
		String getActualBeatmap(@PathParam("beatmapid") int beatmapid);
	}

	protected final ActualBeatmapRepository repository;

	protected final BeatmapDownloader downloader;

	protected final OsuApiBeatmap beatmap;

	@Override
	public String getFile() {
		ActualBeatmap found = repository.findById(beatmap.getBeatmapId()).orElse(null);
		if (found != null && (found.getHash() == null || found.getHash().isEmpty())) {
			found.setHash(md5Hex(found.getContent()));
			repository.save(found);
		}
		if (found == null || (!found.getHash().equals(beatmap.getFileMd5())
				&& found.getDownloaded() < System.currentTimeMillis() - HOURS.toMillis(1))) {
			String downloaded = downloader.getActualBeatmap(beatmap.getBeatmapId());
			if (found == null) {
				found = new ActualBeatmap();
				found.setBeatmapid(beatmap.getBeatmapId());
			}
			found.setContent(downloaded.getBytes(UTF_8));
			found.setDownloaded(System.currentTimeMillis());
			found.setHash(md5Hex(downloaded));
			repository.save(found);
		}
		if (!found.getHash().equals(beatmap.getFileMd5())) {
			throw new WebApplicationException(Response.status(Status.BAD_GATEWAY)
					.entity(format("Beatmap %s is damaged. Expected hash code: %s Actual: %s", beatmap.getBeatmapId(),
							beatmap.getFileMd5(), found.getHash()))
					.build());
		}
		return new String(found.getContent(), UTF_8);
	}

	@Override
	public void setFile(String content) {
		String hash = DigestUtils.md5Hex(content);
		if (!hash.equals(beatmap.getFileMd5())) {
			throw new WebApplicationException(
					String.format("Hash does not match. Expected: %s Actual: %s", beatmap.getFileMd5(), hash),
					Status.FORBIDDEN);
		}
		ActualBeatmap found = repository.findById(beatmap.getBeatmapId()).orElse(null);
		if (found == null) {
			found = new ActualBeatmap();
			found.setBeatmapid(beatmap.getBeatmapId());
		}
		found.setContent(content.getBytes(UTF_8));
		found.setDownloaded(System.currentTimeMillis());
		found.setHash(hash);
		repository.save(found);
	}
}
