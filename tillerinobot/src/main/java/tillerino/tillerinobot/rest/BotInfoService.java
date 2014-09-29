package tillerino.tillerinobot.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.Data;
import tillerino.tillerinobot.BotAPIServer;

@Path("/botinfo")
public class BotInfoService {
	private BotAPIServer server;
	
	public BotInfoService(BotAPIServer server) {
		this.server = server;
	}

	@Data
	public static class BotInfo {
		boolean isConnected;
		long runningSince;
		long lastPingDeath;
		long lastInteraction;
		long lastSentMessage;
	}

	public BotInfo botInfo = new BotInfo();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public BotInfo botinfo() {
		if(server.bot != null) {
			botInfo.isConnected = server.bot.isConnected();
		} else {
			botInfo.isConnected = false;
		}
		return botInfo;
	}

	public void setRunningSince(long runningSince) {
		botInfo.runningSince = runningSince;
	}

	public void setLastPingDeath(long lastPingDeath) {
		botInfo.lastPingDeath = lastPingDeath;
	}

	public void setLastInteraction(long lastInteraction) {
		botInfo.lastInteraction = lastInteraction;
	}

	public void setLastSentMessage(long lastSentMessage) {
		botInfo.lastSentMessage = lastSentMessage;
	}

}
