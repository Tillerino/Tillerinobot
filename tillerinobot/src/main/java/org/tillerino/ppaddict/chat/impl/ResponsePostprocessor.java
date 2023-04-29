package org.tillerino.ppaddict.chat.impl;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.MDC;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Action;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponse.Success;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;
import org.tillerino.ppaddict.chat.GameChatWriter;
import org.tillerino.ppaddict.chat.GameChatWriter.Error;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.local.LocalGameChatMetrics;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;
import org.tillerino.ppaddict.util.Result;
import org.tillerino.ppaddict.util.Result.Err;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Prepares responses to be written out. Writing is done in synchronous fashion
 * to make sure that the bouncer gets all the correct information. It also
 * enables us to switch out the implementation of {@link GameChatWriter}.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject, @SuppressFBWarnings("EI_EXPOSE_REP2")})
public class ResponsePostprocessor implements GameChatResponseConsumer {
	private final Bouncer bouncer;

	private final GameChatWriter writer;

	private final LiveActivity liveActivity;

	private final LocalGameChatMetrics botInfo;

	private final Clock clock;

	@Override
	public void onResponse(GameChatResponse response, GameChatEvent event) throws InterruptedException {
		try {
			for (GameChatResponse r : response.flatten()) {
				for (int i = 0; i < 10; i++) { // arbitrary retry limit greater than zero
					if (handleResponse(r, event) instanceof Err<?, Error> err) {
						if(err.e() instanceof Error.Retry retry) {
							log.warn("Bot not connected. Retrying.");
							Thread.sleep(retry.millis());
							continue;
						} else if (err.e() instanceof Error.Timeout) {
							log.warn("Timed out while trying to send to IRC");
						} else {
							log.error("Unknown error while sending response");
						}
					}
					break;
				}
			}
		} catch (IOException e) {
			log.warn("Error while sending chat message", e);
		}
		if (event.isInteractive() && !bouncer.exit(event.getNick(), event.getEventId()) && !response.isNone()) {
			log.warn("Responded to stale event {} {}", event.getNick(), event.getEventId());
		}
	}

	@SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
	private Result<Optional<String>, Error> handleResponse(GameChatResponse response, GameChatEvent result) throws InterruptedException, IOException {
		if (response instanceof Message message) {
			return message(message.getContent(), false, result);
		} else if (response instanceof Success success) {
			return message(success.getContent(), true, result);
		} else if (response instanceof Action action) {
			String msg = action.getContent();
			return writer.action(msg, result.getNick()).map(ok -> {
				liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
				try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_STATE, "sent")) {
					log.debug("sent action: " + msg);
				} finally {
					// This is possibly set by the writer. If an exception occurs, it is cleared by the queue
					MDC.remove(MdcUtils.MDC_PING);
				}
				return ok;
			});
		} else {
			throw new NotImplementedException("Unknown response type: " + response.getClass());
		}
	}

	private Result<Optional<String>, Error> message(String msg, boolean success, GameChatEvent result) throws InterruptedException, IOException {
		return writer.message(msg, result.getNick()).map(ok -> {
			liveActivity.propagateSentMessage(result.getNick(), result.getEventId());
			try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_STATE, "sent")) {
				if (success) {
					mdc.add(MdcUtils.MDC_DURATION, clock.currentTimeMillis() - result.getTimestamp());
					mdc.add(MdcUtils.MDC_SUCCESS, true);
					mdc.add(MdcUtils.MCD_OSU_API_RATE_BLOCKED_TIME, result.getMeta().getRateLimiterBlockedTime());
					if (Objects.equals(MDC.get(MdcUtils.MDC_HANDLER), MdcUtils.MDC_HANDLER_RECOMMEND)) {
						botInfo.setLastRecommendation(clock.currentTimeMillis());
					}
				}
				log.debug("sent: " + msg);
				botInfo.setLastSentMessage(clock.currentTimeMillis());
			} finally {
				// This is possibly set by the writer. If an exception occurs, it is cleared by the queue
				MDC.remove(MdcUtils.MDC_PING);
			}
			return ok;
		});
	}
}
