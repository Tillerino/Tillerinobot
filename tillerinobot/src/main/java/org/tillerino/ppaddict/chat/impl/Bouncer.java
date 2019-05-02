package org.tillerino.ppaddict.chat.impl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.ppaddict.util.Clock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

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
	@ToString
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	public final class SemaphorePayload {
		private final long eventId;

		private final long enteredTime;

		@Wither(AccessLevel.PRIVATE)
		private final Thread workingThread;

		@Wither(AccessLevel.PRIVATE)
		private final int attemptsSinceEntered;

		@Wither
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
			SemaphorePayload changed = new SemaphorePayload(eventId, clock.currentTimeMillis(), null, 0, false);
			newObject.set(changed);
			return changed;
		}) == newObject.get();
	}

	public boolean exit(String ircNick, long eventId) {
		return updateIfPresent(ircNick, eventId, x -> null);
	}

	public boolean setThread(String ircNick, long eventId) {
		return updateIfPresent(ircNick, eventId, payload -> payload.withWorkingThread(Thread.currentThread()));
	}

	public boolean clearThread(String ircNick, long eventId) {
		return updateIfPresent(ircNick, eventId, payload -> payload.withWorkingThread(null));
	}

	public Optional<Thread> getThread(String ircNick) {
		return Optional.ofNullable(perUserLock.getUnchecked(ircNick).get()).map(p -> p.workingThread);
	}

	public Optional<SemaphorePayload> get(String ircNick) {
		return Optional.ofNullable(getSemaphore(ircNick).get());
	}

	private AtomicReference<SemaphorePayload> getSemaphore(String ircNick) {
		return perUserLock.getUnchecked(ircNick);
	}

	public boolean updateIfPresent(String ircNick, long eventId, UnaryOperator<SemaphorePayload> mapper) {
		AtomicReference<SemaphorePayload> newObject = new AtomicReference<>();
		return getSemaphore(ircNick).updateAndGet(payload -> {
			SemaphorePayload changed = Optional.ofNullable(payload).filter(p -> p.eventId == eventId).map(mapper).orElse(null);
			newObject.set(changed);
			return changed;
		}) == newObject.get();
	}
}
