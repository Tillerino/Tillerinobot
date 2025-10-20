package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LoggingUtilsTest {

    @Test
    public void testGetRandomString() throws Exception {
        assertThat(LoggingUtils.getRandomString(5)).matches("[A-Z]{5}");
    }
}
