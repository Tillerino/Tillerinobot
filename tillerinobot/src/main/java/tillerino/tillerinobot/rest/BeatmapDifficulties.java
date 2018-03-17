package tillerino.tillerinobot.rest;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;

import tillerino.tillerinobot.rest.BeatmapInfoService.BeatmapInfo;

@Path("/beatmapinfo")
public interface BeatmapDifficulties {
	@KeyRequired
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	BeatmapInfo getBeatmapInfo(@QueryParam("beatmapid") @BeatmapId int beatmapid,
			@QueryParam("mods") @BitwiseMods long mods,
			@QueryParam("acc") List<Double> requestedAccs,
			@QueryParam("wait") @DefaultValue("1000") long wait) throws Throwable;
}