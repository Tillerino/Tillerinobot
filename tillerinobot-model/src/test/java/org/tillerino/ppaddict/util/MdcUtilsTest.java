package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.tillerino.ppaddict.util.TestAppender.mdc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.MDC;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;
import org.tillerino.ppaddict.util.MdcUtils.MdcSnapshot;
import org.tillerino.ppaddict.util.TestAppender.LogRule;

@Slf4j
public class MdcUtilsTest {
    @RegisterExtension
    public final LogRule logRule = TestAppender.rule(MdcUtilsTest.class);

    @AfterEach
    public final void tearDown() {
        MDC.clear();
    }

    @Test
    public void keyIsAddedAndRemoved() throws Exception {
        try (var _ = MdcUtils.with("foo", "baz")) {
            assertThat(MDC.get("foo")).isEqualTo("baz");
        }
        assertThat(MDC.get("foo")).isNull();
    }

    @Test
    public void keyIsAddedAndRestored() throws Exception {
        MDC.put("foo", "bar");
        assertThat(MDC.get("foo")).isEqualTo("bar");
        try (var _ = MdcUtils.with("foo", "baz")) {
            assertThat(MDC.get("foo")).isEqualTo("baz");
        }
        assertThat(MDC.get("foo")).isEqualTo("bar");
    }

    @Test
    public void chained() {
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
        try (var _ = snapshot.apply()) {
            assertThat(MDC.get("foo")).isEqualTo("bar");
            assertThat(MDC.get("foos")).isEqualTo("bars");
        }
        assertThat(MDC.get("foo")).isEqualTo("baz");
        assertThat(MDC.get("foos")).isNull();
    }

    @Test
    public void testWithActualLog() {
        MDC.put("foo", "bar");
        log.debug("boink");
        logRule.assertThat().hasSize(1).first().satisfies(mdc("foo", "bar"));
    }

    @Test
    public void eventIdCanBeRetrieved() {
        MDC.clear();
        assertThat(MdcUtils.getLong(MdcUtils.MDC_EVENT)).isEmpty();
        MDC.put("event", "123");
        assertThat(MdcUtils.getLong(MdcUtils.MDC_EVENT)).isPresent().satisfies(o -> assertThat(o.getAsLong())
                .isEqualTo(123L));
    }

    private MdcSnapshot serde(MdcSnapshot s) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final byte[] serialized = mapper.writeValueAsBytes(s);
        return mapper.readerFor(MdcSnapshot.class).readValue(serialized);
    }

    @Test
    public void testIncrementCounter() {
        MDC.put("foo", "123");
        MdcUtils.incrementCounter("foo");
        assertThat(MDC.get("foo")).isEqualTo("124");

        MDC.clear();
        MdcUtils.incrementCounter("foo"); // adds automatically
        assertThat(MDC.get("foo")).isEqualTo("1");

        MDC.put("foo", "bar");
        assertThatThrownBy(() -> MdcUtils.incrementCounter("foo")).isInstanceOf(NumberFormatException.class);
    }
}
