package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivateActionTest {
	@Test
	public void interactive() throws Exception {
		assertThat(new PrivateAction(123, "", 456, "")).hasFieldOrPropertyWithValue("interactive", true);
	}
}
