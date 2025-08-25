package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;

/**
 * Wrapper around {@link LogCaptor} for convenience.
 */
public class TestAppender {
	public static Consumer<LogEvent> mdc(String key, String value) {
		return event -> assertThat(event.getDiagnosticContext().get(key)).isEqualTo(value);
	}

	public static LogRule rule(Class... targets) {
		Validate.exclusiveBetween(0, Integer.MAX_VALUE, targets.length, "Need to specify at least one logger to capture");
		return new LogRule(() -> Stream.of(targets).map(LogCaptor::forClass).toList());
	}

	public static LogRule rule(String... targets) {
		Validate.exclusiveBetween(0, Integer.MAX_VALUE, targets.length, "Need to specify at least one logger to capture");
		return new LogRule(() -> Stream.of(targets).map(LogCaptor::forName).toList());
	}

	public static class LogRule implements BeforeEachCallback, AfterEachCallback {
		private final Supplier<List<LogCaptor>> initializer;

		private List<LogCaptor> logCaptors;

    private LogRule(Supplier<List<LogCaptor>> initializer) {this.initializer = initializer;}

    @Override
		public void beforeEach(ExtensionContext context) {
			logCaptors = initializer.get();
		}

		@Override
		public void afterEach(ExtensionContext context) {
			logCaptors.forEach(LogCaptor::close);
		}

		public void clear() {
			logCaptors.forEach(LogCaptor::clearLogs);
		}

		public ListAssert<LogEvent> assertThat() {
			return Assertions.assertThat(events());
		}

		public List<LogEvent> events() {
			return logCaptors.stream().flatMap((LogCaptor logCaptor) -> logCaptor.getLogEvents().stream()).toList();
		}
	}
}
