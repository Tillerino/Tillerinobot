package tillerino.tillerinobot;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.tillerino.osuApiModel.v2.DownloaderV2;
import org.tillerino.osuApiModel.v2.TokenHelper.Credentials;
import org.tillerino.osuApiModel.v2.TokenHelper.TokenCache;
import org.tillerino.ppaddict.util.MdcUtils;

import dagger.Provides;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.ServiceUnavailableException;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;

/**
 * Implements a client for the v1 API including rate limiting.
 */
@Singleton
public class OsuApiV2 implements OsuApi {
	private final RateLimiter rateLimiter;

	private final DownloaderV2 downloader;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public OsuApiV2(@Named("osuapiv2.url") URI baseUrl,
			TokenCache tokenCache, RateLimiter rateLimiter) {
		this(new DownloaderV2(baseUrl, tokenCache), rateLimiter);
	}

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Wrapping")
	public OsuApiV2(DownloaderV2 downloader, RateLimiter rateLimiter) {
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

	@dagger.Module
	interface CredentialsFromEnvModule {
		@Provides
		static @Named("osuapiv2.url") URI baseUrl() {
			return URI.create(envOrThrow("OSU_API_V2_URL"));
		}

		@Provides
		static Credentials credentials() {
			return Credentials.fromEnvOrProps();
		}

		static String envOrThrow(String name) {
			String env = System.getenv(name);
			if (env == null) {
				throw new NoSuchElementException("Missing environment variable " + name);
			}
			return env;
		}
	}
}
