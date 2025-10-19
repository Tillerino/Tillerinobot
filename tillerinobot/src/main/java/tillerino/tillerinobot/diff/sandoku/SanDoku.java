package tillerino.tillerinobot.diff.sandoku;

import com.github.omkelderman.sandoku.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

/**
 * Client for an instance of
 * https://github.com/omkelderman/SanDoku
 */
public interface SanDoku {
	/**
	 * Once changes in SanDoku have been deployed and values need to be recalculated, bump this version!
	 */
	int VERSION = 2;

	static ProcessorApi defaultClient(URI baseUri) {
		return WebResourceFactory.newResource(ProcessorApi.class, JerseyClientBuilder.createClient().target(baseUri));
	}

	static Optional<ValidationProblemDetails> unwrapError(BadRequestException e) {
		try {
			return Optional.of(e.getResponse().readEntity(ValidationProblemDetails.class));
		} catch (ProcessingException e1) {
			return Optional.empty();
		}
	}

	/**
	 * Implementation of the {@link SanDoku} interface which reads results from the classpath resources.
	 */
	class SanDokuTestImpl implements ProcessorApi {
		@Override
		public DiffResult processorCalcDiff(
				Integer mode,
				Integer mods,
				Boolean storeResultInCacheForPpCalc,
				byte[] beatmap) {
			String md5 = DigestUtils.md5Hex(beatmap);
			try {
				URL url = IOUtils.resourceToURL(String.format("/SanDoku/%s-%s-%s.json", md5, mode, mods));
				return new ObjectMapper().readValue(url, DiffResult.class);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public PpOutput processorCalcPp(String beatmapMd5, Integer mode, Integer mods, ScoreInfo scoreInfo) {
			throw new NotImplementedException(); // we do not use this
		}
	}
}
