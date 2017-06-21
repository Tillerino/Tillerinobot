package tillerino.tillerinobot.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.tillerino.osuApiModel.OsuApiBeatmap;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Path("")
@Api(hidden = true)
@Produces(MediaType.APPLICATION_JSON)
@KeyRequired
public interface BeatmapResource {
	@ApiOperation(value = "Get a beatmap object", tags = "public", authorizations = @Authorization("api_key"))
	@GET
	OsuApiBeatmap get();

	@ApiOperation(value = "Get a beatmap file", tags = "public", authorizations = @Authorization("api_key"))
	@Path("/file")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	String getFile();

	@ApiOperation(value = "Update a beatmap file", tags = "public", authorizations = @Authorization("api_key"))
	@ApiResponses({
		@ApiResponse(code = 403, message = "The supplied beatmap file did not have the required hash value"),
		@ApiResponse(code = 204, message = "Beatmap file saved")
	})
	@Path("/file")
	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	void setFile(String content);
}
