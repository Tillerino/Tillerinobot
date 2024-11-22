package org.tillerino.ppaddict.chat.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.Bouncer.SemaphorePayload;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.ppaddict.util.PhaseTimer;

/**
 * Here we do anything that we can do right after we receive the message and
 * eventually put the message into the event queue. One important job is to ask
 * the {@link Bouncer} if we aren't already processing a message for the user in
 * question. Non-interactive events like joins are passed right through to the
 * message queue.
 */
@Singleton
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessagePreprocessor implements GameChatEventConsumer {
	private final GameChatEventQueue queue;

	private final LiveActivity liveActivity;

	private final Bouncer bouncer;

	private final GameChatResponseQueue responses;

	private final Clock clock;

	@Override
	@SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
	public void onEvent(GameChatEvent event) throws InterruptedException {
		try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_EVENT, event.getEventId())) {
			mdc.add(MdcUtils.MDC_USER, event.getNick());
			event.getMeta().setMdc(MdcUtils.getSnapshot());
			event.getMeta().setTimer(new PhaseTimer());

			if (event instanceof PrivateMessage || event instanceof PrivateAction) {
				liveActivity.propagateReceivedMessage(event.getNick(), event.getEventId());
			}
			if (event instanceof PrivateAction action) {
				try (MdcAttributes mdc2 = MdcUtils.with(MdcUtils.MDC_STATE, "action")) {
					log.debug("action: " + action.getAction());
				}
			}
			if (event instanceof PrivateMessage message) {
				try (MdcAttributes mdc2 = MdcUtils.with(MdcUtils.MDC_STATE, "msg")) {
					log.debug("received: " + message.getMessage());
				}
			}
			if (event.isInteractive() && !bouncer.tryEnter(event.getNick(), event.getEventId())) {
				responses.onResponse(handleSemaphoreInUse(event), event);
				return;
			}

			event.completePhase(PhaseTimer.PREPROCESS);
			queue.onEvent(event);
		}
	}

	private GameChatResponse handleSemaphoreInUse(GameChatEvent event) {
		return bouncer.get(event.getNick()).map(feedback -> {
			String purpose = "Concurrent " + event.getClass().getSimpleName();
			double processing = (clock.currentTimeMillis() - feedback.getEnteredTime()) / 1000d;

			if (processing > 5) {
				log.warn("{} - request has been processing for {}. Currently in queue. Event queue size: {} Response queue size: {}", purpose, processing, queue.size(), responses.size());
				if (!feedback.isWarningSent() && setWarningSent(event, feedback)) {
					return new Message("Just a second...");
				}
			} else {
				log.debug(purpose);
			}
			// only send if thread is not null, i.e. message is not in queue
			if (feedback.getAttemptsSinceEntered() >= 3 && !feedback.isWarningSent() && setWarningSent(event, feedback)) {
				return new Message("[http://i.imgur.com/Ykfua8r.png ...]");
			}

			return GameChatResponse.none();
		}).orElseGet(GameChatResponse::none);
	}

	private boolean setWarningSent(GameChatEvent event, SemaphorePayload feedback) {
		return bouncer.updateIfPresent(event.getNick(), feedback.getEventId(), p -> p.withWarningSent(true));
	}
}
