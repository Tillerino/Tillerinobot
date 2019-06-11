package tillerino.tillerinobot.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import org.tillerino.ppaddict.util.Clock;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import tillerino.tillerinobot.BotRunner;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BotInfoService implements BotStatus {
	private final BotRunner bot;

	private final BotInfo botInfo;

	private final Clock clock;

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
		private long responseQueueSize;
		private long eventQueueSize;
	}

	@Override
	public BotInfo botinfo() {
		botInfo.setConnected(bot.isConnected());
		return botInfo;
	}

	/*
	 * The following are endpoints for automated health checks, so they don't return anything
	 * valuable other than a 200 or 404.
	 */
	@Override
	public boolean isReceiving() {
		if (botInfo.getLastReceivedMessage() < clock.currentTimeMillis() - 10000) {
			throw new NotFoundException();
		}
		return true;
	}

	@Override
	public boolean isSending() {
		if (botInfo.getLastSentMessage() < clock.currentTimeMillis() - 60000) {
			throw new NotFoundException();
		}
		return true;
	}

	@Override
	public boolean isRecommending() {
		if (botInfo.getLastRecommendation() < clock.currentTimeMillis() - 60000) {
			throw new NotFoundException();
		}
		return true;
	}
}
