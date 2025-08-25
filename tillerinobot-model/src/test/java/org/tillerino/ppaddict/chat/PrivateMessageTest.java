package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PrivateMessageTest {
	@Test
	public void interactive() throws Exception {
		assertThat(new PrivateMessage(123, "", 456, "")).hasFieldOrPropertyWithValue("interactive", true);
	}
}
