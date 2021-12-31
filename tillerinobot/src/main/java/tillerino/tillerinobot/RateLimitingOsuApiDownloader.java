package tillerino.tillerinobot;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import jakarta.ws.rs.ServiceUnavailableException;

import org.tillerino.osuApiModel.Downloader;
import org.tillerino.ppaddict.util.MdcUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class RateLimitingOsuApiDownloader extends Downloader {
	private final RateLimiter limiter;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public RateLimitingOsuApiDownloader(@Named("osuapi.url") URL baseUrl,
			@Named("osuapi.key") String key, RateLimiter limiter) {
		super(baseUrl, key);
		this.limiter = limiter;
	}

	@Override
	public JsonNode get(String command, String... parameters) throws IOException {
		try {
			limiter.limitRate();
			MdcUtils.incrementCounter(MdcUtils.MDC_EXTERNAL_API_CALLS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceUnavailableException();
		}
		return super.get(command, parameters);
	}
}
