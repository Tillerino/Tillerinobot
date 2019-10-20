package tillerino.tillerinobot;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ServiceUnavailableException;

import org.tillerino.osuApiModel.Downloader;

import com.google.gson.JsonElement;

public class RateLimitingOsuApiDownloader extends Downloader {
	private final RateLimiter limiter;

	@Inject
	public RateLimitingOsuApiDownloader(@Named("osuapi.url") URL baseUrl,
			@Named("osuapi.key") String key, RateLimiter limiter) {
		super(baseUrl, key);
		this.limiter = limiter;
	}

	@Override
	public JsonElement get(String command, String... parameters) throws IOException {
		try {
			limiter.limitRate();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ServiceUnavailableException();
		}
		return super.get(command, parameters);
	}
}
