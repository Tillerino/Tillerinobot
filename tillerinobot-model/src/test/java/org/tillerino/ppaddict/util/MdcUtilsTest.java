package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.MDC;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;
import org.tillerino.ppaddict.util.MdcUtils.MdcSnapshot;
import org.tillerino.ppaddict.util.TestAppender.LogRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MdcUtilsTest {
	@Rule
	public final LogRule logRule = TestAppender.rule();

	@After
	public final void tearDown() throws Exception {
		MDC.clear();
	}

	@Test
	public void keyIsAddedAndRemoved() throws Exception {
		try (AutoCloseable with = MdcUtils.with("foo", "baz")) {
			assertThat(MDC.get("foo")).isEqualTo("baz");
		}
		assertThat(MDC.get("foo")).isNull();
	}

	@Test
	public void keyIsAddedAndRestored() throws Exception {
		MDC.put("foo", "bar");
		assertThat(MDC.get("foo")).isEqualTo("bar");
		try (AutoCloseable with = MdcUtils.with("foo", "baz")) {
			assertThat(MDC.get("foo")).isEqualTo("baz");
		}
		assertThat(MDC.get("foo")).isEqualTo("bar");
	}

	@Test
	public void chained() throws Exception {
		MDC.put("foo", "bar");
		MDC.put("foos", "bars");
		try (MdcAttributes with = MdcUtils.with("foo", "baz")) {
			with.add("foos", "bazs");
			assertThat(MDC.get("foo")).isEqualTo("baz");
			assertThat(MDC.get("foos")).isEqualTo("bazs");
		}
		assertThat(MDC.get("foo")).isEqualTo("bar");
		assertThat(MDC.get("foos")).isEqualTo("bars");
	}

	@Test
	public void snapShot() throws Exception {
		MDC.put("foo", "bar");
		MDC.put("foos", "bars");
		MdcSnapshot snapshot = serde(MdcUtils.getSnapshot());
		MDC.put("foo", "baz");
		MDC.remove("foos");
		try (MdcAttributes with = snapshot.apply()) {
			assertThat(MDC.get("foo")).isEqualTo("bar");
			assertThat(MDC.get("foos")).isEqualTo("bars");
		}
		assertThat(MDC.get("foo")).isEqualTo("baz");
		assertThat(MDC.get("foos")).isNull();
	}

	@Test
	public void testWithActualLog() throws Exception {
		MDC.put("foo", "bar");
		log.debug("boink");
		logRule.assertThat()
			.hasSize(1)
			.first().satisfies(x -> assertThat(x.getMDC("foo")).isEqualTo("bar"));
	}

	@Test
	public void eventIdCanBeRetrieved() throws Exception {
		MDC.clear();
		assertThat(MdcUtils.getEventId()).isEmpty();
		MDC.put("event", "123");
		assertThat(MdcUtils.getEventId()).isPresent().satisfies(o -> assertThat(o.getAsLong()).isEqualTo(123L));
	}

	private MdcSnapshot serde(MdcSnapshot s) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final byte[] serialized = mapper.writeValueAsBytes(s);
		return mapper.readerFor(MdcSnapshot.class).readValue(serialized);
	}
}
