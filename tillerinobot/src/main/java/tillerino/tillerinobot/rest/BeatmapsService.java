package tillerino.tillerinobot.rest;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@Path("/beatmaps")
public interface BeatmapsService {
	@Path("/byId/{id}")
	@ApiOperation("")
	BeatmapResource byId(@PathParam("id") int id);

	@Path("byHash/{hash}")
	@ApiOperation("")
	BeatmapResource byHash(@PathParam("hash") String hash);
}
