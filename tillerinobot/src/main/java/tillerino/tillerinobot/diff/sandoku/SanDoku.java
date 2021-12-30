package tillerino.tillerinobot.diff.sandoku;

import java.net.URI;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.GameMode;

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
	@Consumes("plain/osu")
	SanDokuResponse getDiff(@GameMode @QueryParam("mode") int gameMode, @BitwiseMods @QueryParam("mods") long mods, byte[] beatmap);

	static SanDoku defaultClient(URI baseUri) {
		return WebResourceFactory.newResource(SanDoku.class, JerseyClientBuilder.createClient().target(baseUri));
	}
}
