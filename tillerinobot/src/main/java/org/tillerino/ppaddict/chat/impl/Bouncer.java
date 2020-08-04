package org.tillerino.ppaddict.chat.impl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.util.Clock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;

/**
 * This guy ensures that only a single interactive request from each user is
 * worked on at a time. When a message is received,
 * {@link #tryAcquire(String, long)} is called. If the return value is false,
 * the message is dropped. After a message is responded to,
 * {@link #release(String, long)} is called to announce that the machine is now
 * ready work work on messages for that user again. For informational purposes,
 * additional fields can be set like the thread that is currently working on
 * that message.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Bouncer {
	@RequiredArgsConstructor(access = AccessLevel.PACKAGE) // visible for testing
	@Getter
	@ToString
	@EqualsAndHashCode
	public static final class SemaphorePayload {
		private final long eventId;

		private final long enteredTime;

		@With(AccessLevel.PRIVATE)
		private final int attemptsSinceEntered;

		@With
		private final boolean warningSent;
	}

	private final LoadingCache<String, AtomicReference<SemaphorePayload>> perUserLock = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
			.build(CacheLoader.from(() -> new AtomicReference<>()));

	private final Clock clock;

	public boolean tryEnter(String ircNick, long eventId) {
		AtomicReference<SemaphorePayload> newObject = new AtomicReference<>();
		return getSemaphore(ircNick).updateAndGet(payload -> {
			if (payload != null) {
				return payload.withAttemptsSinceEntered(payload.attemptsSinceEntered + 1);
			}
			SemaphorePayload changed = new SemaphorePayload(eventId, clock.currentTimeMillis(), 0, false);
			newObject.set(changed);
			return changed;
		}) == newObject.get();
	}

	public boolean exit(String ircNick, long eventId) {
		return updateIfPresent(ircNick, eventId, x -> null);
	}

	public Optional<SemaphorePayload> get(String ircNick) {
		return Optional.ofNullable(getSemaphore(ircNick).get());
	}

	private AtomicReference<SemaphorePayload> getSemaphore(String ircNick) {
		return perUserLock.getUnchecked(ircNick);
	}

	/**
	 * Updates the current payload.
	 *
	 * @param ircNick the nick name to upload the payload for
	 * @param eventId the event ID. If the current payload has a different event ID, no change will be applied.
	 * @param mapper change to the underlying object. May return null to remove the payload.
	 * @return true if the payload after method call was returned by the mapper.
	 */
	public boolean updateIfPresent(String ircNick, long eventId, UnaryOperator<SemaphorePayload> mapper) {
		AtomicBoolean changed = new AtomicBoolean();
		getSemaphore(ircNick).updateAndGet(payload -> {
			changed.set(false);
			if (payload == null || payload.eventId != eventId) {
				return payload;
			}
			changed.set(true);
			return mapper.apply(payload);
		});
		return changed.get();
	}
}
