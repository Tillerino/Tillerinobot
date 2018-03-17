package tillerino.tillerinobot.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import org.pircbotx.PircBotX;

import lombok.Data;
import tillerino.tillerinobot.BotRunner;

@Singleton
public class BotInfoService implements BotStatus {
	private final BotRunner bot;

	@Inject
	public BotInfoService(BotRunner bot, BotInfo botInfo) {
		super();
		this.bot = bot;
		this.botInfo = botInfo;
	}

	@Data
	@Singleton
	public static class BotInfo {
		private boolean isConnected;
		private long runningSince;
		private long lastPingDeath;
		private long lastInteraction;
		private long lastReceivedMessage;
		private long lastSentMessage;
		private long lastRecommendation;
	}

	private final BotInfo botInfo;

	@Override
	public BotInfo botinfo() {
		PircBotX pircBot = bot.getBot();
		if (pircBot != null) {
			botInfo.setConnected(pircBot.isConnected());
		} else {
			botInfo.setConnected(false);
		}
		return botInfo;
	}

	/*
	 * The following are endpoints for automated health checks, so they don't return anything
	 * valuable other than a 200 or 404.
	 */
	@Override
	public boolean isReceiving() {
		if (botInfo.getLastReceivedMessage() < System.currentTimeMillis() - 10000) {
			throw new NotFoundException();
		}
		return true;
	}

	@Override
	public boolean isSending() {
		if (botInfo.getLastSentMessage() < System.currentTimeMillis() - 60000) {
			throw new NotFoundException();
		}
		return true;
	}

	@Override
	public boolean isRecommending() {
		if (botInfo.getLastRecommendation() < System.currentTimeMillis() - 60000) {
			throw new NotFoundException();
		}
		return true;
	}
}
