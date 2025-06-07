package tillerino.tillerinobot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.ServiceUnavailableException;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.ppaddict.util.MdcUtils;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Implements a client for the v1 API including rate limiting.
 */
public class OsuApiV1 implements OsuApi {
	private final RateLimiter rateLimiter;

	private final Downloader downloader;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public OsuApiV1(@Named("osuapi.url") URL baseUrl,
			@Named("osuapi.key") String key, RateLimiter rateLimiter) {
		this(new Downloader(baseUrl, key), rateLimiter);
	}

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Wrapping")
	public OsuApiV1(Downloader downloader, RateLimiter rateLimiter) {
		this.downloader = downloader;
		this.rateLimiter = rateLimiter;
	}

	@Override
	public ApiUser getUser(int userId, int gameMode) throws IOException {
		limitRate();
		return downloader.getUser(userId, gameMode, ApiUser.class);
	}

	@Override
	public ApiUser getUser(String username, int mode) throws IOException {
		limitRate();
		return downloader.getUser(username, mode, ApiUser.class);
	}

	@Override
	public ApiBeatmap getBeatmap(int beatmapid, long mods) throws IOException {
		limitRate();
		return downloader.getBeatmap(beatmapid, mods, ApiBeatmap.class);
	}

	@Override
	public List<ApiScore> getUserTop(int userId, int mode, int limit) throws IOException {
		limitRate();
		return downloader.getUserTop(userId, mode, limit, ApiScore.class);
	}

	@Override
	public List<ApiScore> getUserRecent(int userid, int mode) throws IOException {
		limitRate();
		return downloader.getUserRecent(userid, mode, ApiScore.class);
	}

	private void limitRate() {
		try {
			rateLimiter.limitRate();
			MdcUtils.incrementCounter(MdcUtils.MDC_EXTERNAL_API_CALLS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceUnavailableException();
		}
	}

	/**
	 * see {@link Downloader#createTestDownloader(Class)}
	 */
	public static OsuApiV1 createTestApi(Class<?> cls) {
		return new OsuApiV1(Downloader.createTestDownloader(cls), RateLimiter.unlimited());
	}
}
