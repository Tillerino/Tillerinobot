package tillerino.tillerinobot.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.tillerino.ppaddict.chat.GameChatMetrics;

@Path("/botinfo")
public interface BotStatus {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	GameChatMetrics botinfo();

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