package org.tillerino.ppaddict.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import javax.annotation.CheckForNull;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * With the {@link MDC} we have two problems that originate from the fact that
 * the MDC is bound to a thread:
 * 1) We want to clean up everything that we put in there so that it doesn't
 * leak into a different context that our thread might be working in later.
 * 2) When we transfer a workload into a different thread using a thread pool
 * or some sort of queue, we need to make sure that our MDC travels with the
 * work instead of sticking to the thread.
 */
public class MdcUtils {
	public interface MdcSnapshot {
		/**
		 * Applies the snapshot to the current MDC and returns a closer, which
		 * will restore the previous state. Keep the result in a try-with clause.
		 */
		MdcAttributes apply();

		@JsonValue
		Map<String, String> mdcValues();

		@JsonCreator
		static MdcSnapshot create(Map<String, String> snapshot) {
			return new MdcSnapshotImpl(snapshot);
		}
	}

	/**
	 * Implements a mechanism that cleans up changes to the MDC. See
	 * {@link MdcUtils#with(String, Object)}.
	 */
	public static final class MdcAttributes implements QuietCloseable {
		private Map<String, String> restore = null;

		private String restoreKey = null;
		private String restoreValue = null;

		private MdcAttributes() {
			// empty
		}

		private MdcAttributes(String key, Object value) {
			this.restoreKey = key;
			this.restoreValue = MDC.get(key);
			MDC.put(key, String.valueOf(value));
		}

		@Override
		public void close() {
			if (restore != null) {
				restore.forEach(this::restore);
			}
			if (restoreKey != null) {
				restore(restoreKey, restoreValue);
			}
		}

		private void restore(String key, String value) {
			MDC.remove(key);
			if (value != null) {
				MDC.put(key, value);
			}
		}

		/**
		 * Changes a value in the MDC and stores the original value for clean up
		 * in {@link #close()}.
		 *
		 * @param key
		 *            the key for {@link MDC#put(String, String)}.
		 * @param val
		 *            the value for {@link MDC#put(String, String)}. This
		 *            argument is passed to {@link String#valueOf(Object)},
		 *            which turns null into the string literal "null".
		 */
		public void add(String key, Object val) {
			if (restore == null) {
				restore = new LinkedHashMap<>();
			}
			restore.putIfAbsent(key, MDC.get(key));
			MDC.put(key, String.valueOf(val));
		}
	}

	/**
	 * Takes a snapshot of the current MDC to transfer to a different thread. See {@link MdcSnapshot#apply()}.
	 */
	public static MdcSnapshot getSnapshot() {
		Map<String, String> snapshot = Optional.ofNullable(MDC.getCopyOfContextMap()).orElseGet(Collections::emptyMap);
		return new MdcSnapshotImpl(snapshot);
	}

	/**
	 * Puts a value in the MDC and returns a closeable to restore the current
	 * value in the MDC.
	 * 
	 * @param key
	 *            the key for {@link MDC#put(String, String)}.
	 * @param val
	 *            the value for {@link MDC#put(String, String)}. This argument
	 *            is passed to {@link String#valueOf(Object)}, which turns null
	 *            into the string literal "null".
	 * @return use this as a try-with resource. You can pile on more changes to
	 *         the MDC with {@link MdcAttributes#add(String, Object)}.
	 */
	public static MdcAttributes with(String key, @CheckForNull Object val) {
		if (val != null) {
			return new MdcAttributes(key, val);
		}
		return new MdcAttributes();
	}

	public static OptionalLong getLong(String parameterName) throws NumberFormatException {
		return Optional.ofNullable(MDC.get(parameterName))
			.map(s -> OptionalLong.of(Long.parseLong(s)))
			.orElseGet(OptionalLong::empty);
	}

	public static OptionalInt getInt(String parameterName) throws NumberFormatException {
		return Optional.ofNullable(MDC.get(parameterName))
				.map(s -> OptionalInt.of(Integer.parseInt(s)))
				.orElseGet(OptionalInt::empty);
	}

	public static void incrementCounter(String parameterName) throws NumberFormatException {
		MDC.put(parameterName, Long.toString(getLong(parameterName).orElse(0) + 1));
	}

	public static final String MDC_API_KEY = "apiKey";
	public static final String MDC_API_PATH = "apiPath";
	public static final String MDC_API_STATUS = "apiStatus";
	// beatmapid
	public static final String MDC_DURATION = "duration";
	public static final String MDC_EVENT = "event";
	public static final String MDC_HANDLER = "handler";
	public static final String MCD_OSU_API_RATE_BLOCKED_TIME = "osuApiRateBlockedTime";
	public static final String MDC_PING = "ping";
	public static final String MDC_STATE = "state";
	public static final String MDC_SUCCESS = "success";
	public static final String MDC_THREAD_PRIORITY = "threadPriority";
	public static final String MDC_USER = "user";
	public static final String MDC_EXTERNAL_API_CALLS = "externalApiCalls";
	public static final String MDC_ENGINE = "engine";

	public static final String MDC_HANDLER_RECOMMEND = "r";

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private static final class MdcSnapshotImpl implements MdcSnapshot {
		private final Map<String, String> snapshot;

		@Override
		public MdcAttributes apply() {
			MdcAttributes attributes = new MdcAttributes();
			snapshot.forEach(attributes::add);
			return attributes;
		}

		@Override
		public Map<String, String> mdcValues() {
			return Collections.unmodifiableMap(snapshot);
		}

		@Override
		public String toString() {
			return snapshot.toString();
		}
	}
}
