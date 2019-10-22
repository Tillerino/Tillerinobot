package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatMetrics;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.CommandHandler.Action;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Success;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;

/**
 * Prepares responses to be written out. Writing is done in synchronous fashion
 * to make sure that the bouncer gets all the correct information. It also
 * enables us to switch out the implementation of {@link GameChatWriter}.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ResponsePostprocessor implements GameChatResponseConsumer {
	private final Bouncer bouncer;

	private final GameChatWriter writer;

	private final LiveActivityEndpoint liveActivity;

	private final GameChatMetrics botInfo;

	private final Clock clock;

	@Override
	public void onResponse(GameChatResponse response, GameChatEvent event) throws InterruptedException {
		try {
			for (GameChatResponse r : response) {
				handleResponse(r, event);
			}
		} catch (IOException e) {
			log.warn("Error while sending chat message", e);
		}
		if (event.isInteractive() && !bouncer.exit(event.getNick(), event.getEventId())) {
			log.warn("Responded to stale event {} {}", event.getNick(), event.getEventId());
		}
	}

	private void handleResponse(GameChatResponse response, GameChatEvent result) throws InterruptedException, IOException {
		if (response instanceof Message) {
			message(((Message) response).getContent(), false, result);
		} else if (response instanceof Success) {
			message(((Success) response).getContent(), true, result);
		} else if (response instanceof Action) {
			String msg = ((Action) response).getContent();
			writer.action(msg, result);

			liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
			try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_STATE, "sent")) {
				log.debug("sent action: " + msg);
			} finally {
				// This is possibly set by the writer. If an exception occurs, it is cleared by the queue
				MDC.remove(MdcUtils.MDC_PING);
			}
		} else {
			throw new NotImplementedException("Unknown response type: " + response.getClass());
		}
	}

	private void message(String msg, boolean success, GameChatEvent result) throws InterruptedException, IOException {
		writer.message(msg, result);
		liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
		try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_STATE, "sent")) {
			if (success) {
				mdc.add(MdcUtils.MDC_DURATION, clock.currentTimeMillis() - result.getTimestamp());
				mdc.add(MdcUtils.MDC_SUCCESS, true);
				mdc.add(MdcUtils.MCD_OSU_API_RATE_BLOCKED_TIME, result.getMeta().getRateLimiterBlockedTime());
				if (Objects.equals(MDC.get(MdcUtils.MDC_HANDLER), RecommendHandler.MDC_FLAG)) {
					botInfo.setLastRecommendation(clock.currentTimeMillis());
				}
			}
			log.debug("sent: " + msg);
			botInfo.setLastSentMessage(clock.currentTimeMillis());
		} finally {
			// This is possibly set by the writer. If an exception occurs, it is cleared by the queue
			MDC.remove(MdcUtils.MDC_PING);
		}
	}
}
