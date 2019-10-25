package tillerino.tillerinobot.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import org.tillerino.ppaddict.chat.GameChatClient;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.util.Clock;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BotInfoService implements BotStatus {
	private final GameChatClient bot;

	private final GameChatMetrics botInfo;

	private final Clock clock;

	@Override
	public GameChatMetrics botinfo() {
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
