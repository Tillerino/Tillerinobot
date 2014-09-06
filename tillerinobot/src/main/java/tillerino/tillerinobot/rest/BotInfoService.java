package tillerino.tillerinobot.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
	}

	public BotInfo botInfo = new BotInfo();

	@GET
	@Produces("application/json")
	public BotInfo botinfo() {
		if(true) {
			throw BotAPIServer.getNotFound("something");
		}
		
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

}
