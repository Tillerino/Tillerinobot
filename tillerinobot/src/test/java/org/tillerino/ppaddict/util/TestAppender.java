package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

/**
 * Extends the regular {@link ConsoleAppender} so that log events are collected
 * and can be run assertions against. Use {@link #rule()} as a JUnit
 * {@link Rule} to capture the precise events that were logged during test
 * execution.
 */
public class TestAppender extends ConsoleAppender {
	private static final List<LoggingEvent> events = new ArrayList<>();

	@Override
	public synchronized void append(LoggingEvent event) {
		// make sure that the MDC is copied. Otherwise we'll look up the MDC of
		// the test rather than the event when doing assertions on the Event.
		event.getMDCCopy();
		super.append(event);
		events.add(event);
	}

	public static Consumer<LoggingEvent> mdc(String key, String value) {
		return event -> assertThat(event.getMDC(key)).isEqualTo(value);
	}

	public static LogRule rule() {
		return new LogRule();
	}

	public static class LogRule extends ExternalResource {
		@Override
		protected void before() throws Throwable {
			events.clear();
		}

		@Override
		protected void after() {
			events.clear();
		}

		public void clear() {
			events.clear();
		}

		public ListAssert<LoggingEvent> assertThat() {
			return Assertions.assertThat(events);
		}
	}
}
