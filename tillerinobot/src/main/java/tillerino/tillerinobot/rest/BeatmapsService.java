package tillerino.tillerinobot.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.tillerino.osuApiModel.types.BeatmapId;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
@Path("/beatmaps")
public interface BeatmapsService {
	@Path("/byId/{id}")
	@ApiOperation("")
	BeatmapResource byId(@PathParam("id") @BeatmapId int id);

	@Path("byHash/{hash}")
	@ApiOperation("")
	BeatmapResource byHash(@PathParam("hash") String hash);
}
