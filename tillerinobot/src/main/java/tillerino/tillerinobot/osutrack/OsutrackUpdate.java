package tillerino.tillerinobot.osutrack;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("update")
public interface OsutrackUpdate {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    UpdateResult getUpdate(@QueryParam("user") int osuUserId);
}
