package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class JoinedTest {
    @Test
    public void notInteractive() {
        assertThat(new Joined(123, "", 456)).hasFieldOrPropertyWithValue("interactive", false);
    }
}
