package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.CommandHandler.ResponseList;
import tillerino.tillerinobot.CommandHandler.Success;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.rest.BotInfoService.BotInfo;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ResponsePostprocessor implements GameChatResponseConsumer {
	private final Bouncer bouncer;

	private final GameChatWriter writer;

	private final LiveActivityEndpoint liveActivity;

	private final BotInfo botInfo;

	@Override
	public void onResponse(Response response, GameChatEvent event) throws InterruptedException {
		try {
			handleResponse(response, event);
		} catch (IOException e) {
			log.warn("Error while sending chat message", e);
		}
		if (!bouncer.exit(event.getNick(), event.getEventId())) {
			log.warn("Responded to stale event");
		}
	}

	protected void handleResponse(Response response, GameChatEvent result) throws InterruptedException, IOException {
		if (response instanceof ResponseList) {
			for (Response r : ((ResponseList) response).getResponses()) {
				handleResponse(r, result);
			}
		}
		if (response instanceof Message) {
			message(((Message) response).getContent(), false, result);
		}
		if (response instanceof Success) {
			message(((Success) response).getContent(), true, result);
		}
		if (response instanceof Action) {
			String msg = ((Action) response).getContent();
			writer.action(msg, result);

			liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
			try (MdcAttributes mdc = MdcUtils.with(IRCBot.MDC_STATE, "sent")) {
				log.debug("sent action: " + msg);
			}
		}
	}

	private void message(String msg, boolean success, GameChatEvent result) throws InterruptedException, IOException {
		writer.message(msg, result);
		liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
		try (MdcAttributes mdc = MdcUtils.with(IRCBot.MDC_STATE, "sent")) {
			if (success) {
				mdc.add(MdcUtils.MDC_DURATION, System.currentTimeMillis() - result.getTimestamp());
				mdc.add(MdcUtils.MDC_SUCCESS, true);
				mdc.add(MdcUtils.MCD_OSU_API_RATE_BLOCKED_TIME, result.getMeta().getRateLimiterBlockedTime());
				if (Objects.equals(MDC.get(IRCBot.MDC_HANDLER), RecommendHandler.MDC_FLAG)) {
					botInfo.setLastRecommendation(System.currentTimeMillis());
				}
			}
			log.debug("sent: " + msg);
			botInfo.setLastSentMessage(System.currentTimeMillis());
		}
	}
}
