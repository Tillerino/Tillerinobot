package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SightedTest {
    @Test
    public void notInteractive() {
        assertThat(new Sighted(123, "", 456)).hasFieldOrPropertyWithValue("interactive", false);
    }
}
