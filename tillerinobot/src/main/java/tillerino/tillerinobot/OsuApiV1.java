package tillerino.tillerinobot;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.tillerino.osuApiModel.Downloader;
import org.tillerino.ppaddict.util.MdcUtils;
import tillerino.tillerinobot.data.ApiBeatmap;
import tillerino.tillerinobot.data.ApiScore;
import tillerino.tillerinobot.data.ApiUser;

/** Implements a client for the v1 API including rate limiting. */
@Singleton
public class OsuApiV1 implements OsuApi {
    private final RateLimiter rateLimiter;

    private final Downloader downloader;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
    @Inject
    public OsuApiV1(@Named("osuapi.url") URL baseUrl, @Named("osuapi.key") String key, RateLimiter rateLimiter) {
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

    @dagger.Module
    public interface Module {
        @dagger.Binds
        OsuApi osuApi(OsuApiV1 osuApiV1);
    }

    @dagger.Module
    public interface DownloaderModule {
        @dagger.Provides
        @Named("osuapi.key")
        static String getOsuApiKey() {
            String env = System.getenv("OSUAPIKEY");
            if (env != null) {
                return env;
            }
            InputStream is = DownloaderModule.class.getResourceAsStream("/osuapikey");
            if (is == null) {
                throw new RuntimeException("cannot find osu api key");
            }
            try {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("error reading osu api key", e);
            }
        }

        @dagger.Provides
        @Named("osuapi.url")
        @SneakyThrows
        static URL getOsuApiUrl() {
            String env = System.getenv("OSUAPI_URL");
            if (env != null) {
                if (!env.endsWith("/")) {
                    throw new MalformedURLException("osu API URL must end with a slash");
                }
                return URI.create(env).toURL();
            }
            return URI.create("https://osu.ppy.sh/api/").toURL();
        }
    }
}
