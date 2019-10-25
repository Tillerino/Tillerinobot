package org.tillerino.ppaddict.chat.impl;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatEventQueue;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.impl.Bouncer.SemaphorePayload;
import org.tillerino.ppaddict.util.Clock;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.websocket.LiveActivityEndpoint;

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

	private final LiveActivityEndpoint liveActivity;

	private final Bouncer bouncer;

	private final GameChatResponseQueue responses;

	private final Clock clock;

	@Override
	@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "For the try-with. Looks like this is a Java 13 bug in Spotbugs 3.1.11")
	public void onEvent(GameChatEvent event) throws InterruptedException {
		try (MdcAttributes mdc = MdcUtils.with(MdcUtils.MDC_EVENT, event.getEventId())) {
			mdc.add(MdcUtils.MDC_USER, event.getNick());
			// these fields are also set by the IRC connector, but we'll make sure here

			if (event instanceof PrivateMessage || event instanceof PrivateAction) {
				liveActivity.propagateReceivedMessage(event.getNick(), event.getEventId());
			}
			if (event instanceof PrivateAction) {
				try (MdcAttributes mdc2 = MdcUtils.with(MdcUtils.MDC_STATE, "action")) {
					log.debug("action: " + ((PrivateAction) event).getAction());
				}
			}
			if (event instanceof PrivateMessage) {
				try (MdcAttributes mdc2 = MdcUtils.with(MdcUtils.MDC_STATE, "msg")) {
					log.debug("received: " + ((PrivateMessage) event).getMessage());
				}
			}
			if (event.isInteractive() && !bouncer.tryEnter(event.getNick(), event.getEventId())) {
				responses.onResponse(handleSemaphoreInUse(event), event);
				return;
			}
			queue.onEvent(event);
		}
	}

	private GameChatResponse handleSemaphoreInUse(GameChatEvent event) {
		return bouncer.get(event.getNick()).map(feedback -> {
			String purpose = "Concurrent " + event.getClass().getSimpleName();
			double processing = (clock.currentTimeMillis() - feedback.getEnteredTime()) / 1000d;
			Thread thread = feedback.getWorkingThread();

			if (processing > 5) {
				if (thread != null) {
					StackTraceElement[] stackTrace = thread.getStackTrace();
					stackTrace = Stream.of(stackTrace).filter(elem -> elem.getClassName().contains("tillerino")).toArray(StackTraceElement[]::new);
					Throwable t = new Throwable("Processing thread's stack trace");
					t.setStackTrace(stackTrace);
					log.warn(purpose + " - request has been processing for " + processing, t);
				} else {
					log.warn("{} - request has been processing for {}. Currently in queue. Event queue size: {} Response queue size: {}", purpose, processing, queue.size(), responses.size());
				}
				if (!feedback.isWarningSent() && thread != null && setWarningSent(event, feedback)) {
					return new Message("Just a second...");
				}
			} else {
				log.debug(purpose);
			}
			// only send if thread is not null, i.e. message is not in queue
			if (feedback.getAttemptsSinceEntered() >= 3 && !feedback.isWarningSent() && thread != null && setWarningSent(event, feedback)) {
				return new Message("[http://i.imgur.com/Ykfua8r.png ...]");
			}

			return GameChatResponse.none();
		}).orElseGet(GameChatResponse::none);
	}

	private boolean setWarningSent(GameChatEvent event, SemaphorePayload feedback) {
		return bouncer.updateIfPresent(event.getNick(), feedback.getEventId(), p -> p.withWarningSent(true));
	}
}
