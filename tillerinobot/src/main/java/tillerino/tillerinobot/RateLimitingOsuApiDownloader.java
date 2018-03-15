package tillerino.tillerinobot;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;

import org.tillerino.osuApiModel.Downloader;

import com.google.gson.JsonElement;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RateLimitingOsuApiDownloader extends Downloader {
	private final RateLimiter limiter;

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
