package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * Extends the regular {@link ConsoleAppender} so that log events are collected
 * and can be run assertions against. Use {@link #rule()} as a JUnit
 * {@link Rule} to capture the precise events that were logged during test
 * execution.
 */
@Plugin(name = "TestAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class TestAppender extends AbstractAppender {
	private TestAppender(String name, Filter filter) {
		super(name, filter, null, false, null);
	}

	@PluginFactory
	public static TestAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Filter") Filter filter) {
		return new TestAppender(name, filter);
	}

	private static final List<LogEventWithMdc> events = new ArrayList<>();

	@Override
	public synchronized void append(LogEvent event) {
		// make sure that the MDC is copied. Otherwise we'll look up the MDC of
		// the test rather than the event when doing assertions on the Event.
		synchronized (events) {
			events.add(new LogEventWithMdc(event.toImmutable()));
		}
	}

	public static Consumer<LogEventWithMdc> mdc(String key, String value) {
		return event -> assertThat(event.getContextData().<String> getValue(key)).isEqualTo(value);
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

		public ListAssert<LogEventWithMdc> assertThat() {
			return Assertions.assertThat(events());
		}

		public List<LogEventWithMdc> events() {
			synchronized (events) {
				return Collections.unmodifiableList(new ArrayList<>(events));
			}
		}
	}

	@RequiredArgsConstructor
	public class LogEventWithMdc implements LogEvent {
		@Delegate(types = LogEvent.class)
		final LogEvent wrapped;

		public String getMDC(String string) {
			return wrapped.getContextData().getValue(string);
		}

		@Override
		public String toString() {
			return wrapped.toString();
		}
	}
}
