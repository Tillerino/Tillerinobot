package tillerino.tillerinobot.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.pircbotx.PircBotX;

import lombok.Data;
import tillerino.tillerinobot.BotRunner;

@Singleton
@Path("/botinfo")
public class BotInfoService {
	private BotRunner bot;

	@Inject
	public BotInfoService(BotRunner bot) {
		super();
		this.bot = bot;
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
		PircBotX pircBot = bot.getBot();
		if (pircBot != null) {
			botInfo.isConnected = pircBot.isConnected();
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
