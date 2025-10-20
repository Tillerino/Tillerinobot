package tillerino.tillerinobot.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;

@Path("/botinfo")
public interface BotStatus {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    LocalGameChatMetrics botinfo();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/isReceiving")
    boolean isReceiving();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/isSending")
    boolean isSending();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/isRecommending")
    boolean isRecommending();
}
