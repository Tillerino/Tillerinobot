package org.tillerino.ppaddict.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import org.slf4j.MDC;

import tillerino.tillerinobot.CommandHandler;

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
	}

	/**
	 * Implements a mechanism that cleans up changes to the MDC. See
	 * {@link MdcUtils#with(String, Object)}.
	 */
	public static final class MdcAttributes implements AutoCloseable {
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
		return () -> {
			MdcAttributes attributes = new MdcAttributes();
			snapshot.forEach(attributes::add);
			return attributes;
		};
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
	public static MdcAttributes with(String key, Object val) {
		return new MdcAttributes(key, val);
	}

	public static final String MDC_EVENT = "event";

	/**
	 * I didn't want to rewrite the signature of
	 * {@link CommandHandler#handle(String, org.tillerino.osuApiModel.OsuApiUser, tillerino.tillerinobot.UserDataManager.UserData)},
	 * but we need the event ID in some of the handlers. So we're using this little hack, which uses the MDC to get the ID.
	 */
	public static OptionalLong getEventId() {
		String asString = MDC.get(MDC_EVENT);
		if (asString == null) {
			return OptionalLong.empty();
		}
		return OptionalLong.of(Long.parseLong(asString));
	}

	public static final String MDC_DURATION = "duration";
	public static final String MCD_OSU_API_RATE_BLOCKED_TIME = "osuApiRateBlockedTime";
	public static final String MDC_SUCCESS = "success";
}
