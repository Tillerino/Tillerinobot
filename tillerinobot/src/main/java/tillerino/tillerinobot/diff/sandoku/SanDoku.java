package tillerino.tillerinobot.diff.sandoku;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client for an instance of
 * https://github.com/omkelderman/SanDoku
 */
public interface SanDoku {
	/**
	 * Once changes in SanDoku have been deployed and values need to be recalculated, bump this version!
	 */
	final int VERSION = 1;

	@POST
	@Path("/diff")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes("text/osu")
	SanDokuResponse getDiff(@GameMode @QueryParam("mode") int gameMode, @BitwiseMods @QueryParam("mods") long mods, byte[] beatmap);

	static SanDoku defaultClient(URI baseUri) {
		return WebResourceFactory.newResource(SanDoku.class, JerseyClientBuilder.createClient().target(baseUri));
	}

	static Optional<SanDokuError> unwrapError(BadRequestException e) {
		try {
			return Optional.of(e.getResponse().readEntity(SanDokuError.class));
		} catch (ProcessingException e1) {
			return Optional.empty();
		}
	}

	/**
	 * Implementation of the {@link SanDoku} interface which reads results from the classpath resources.
	 */
	class SanDokuTestImpl implements SanDoku {
		@Override
		public SanDokuResponse getDiff(int gameMode, long mods, byte[] beatmap) {
			String md5 = DigestUtils.md5Hex(beatmap);
			try {
				URL url = IOUtils.resourceToURL(String.format("/SanDoku/%s-%s-%s.json", md5, gameMode, mods));
				return new ObjectMapper().readValue(url, SanDokuResponse.class);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
