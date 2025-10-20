package tillerino.tillerinobot.rest;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import tillerino.tillerinobot.rest.BeatmapInfoService.BeatmapInfo;

@Path("/beatmapinfo")
public interface BeatmapDifficulties {
    @KeyRequired
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    BeatmapInfo getBeatmapInfo(
            @QueryParam("beatmapid") @BeatmapId int beatmapid,
            @QueryParam("mods") @BitwiseMods long mods,
            @QueryParam("acc") List<Double> requestedAccs,
            @QueryParam("wait") @DefaultValue("1000") long wait)
            throws Throwable;
}
