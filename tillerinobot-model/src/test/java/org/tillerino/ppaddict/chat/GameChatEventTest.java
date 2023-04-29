package org.tillerino.ppaddict.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GameChatEventTest {
	@Test
	public void joinedSerialization() throws Exception {
		String expected = """
				{"@c":".Joined","eventId":123,"nick":"nick","timestamp":456,"meta":{"rateLimiterBlockedTime":0,"mdc":null}}
			""";
		Joined joined = new Joined(123, "nick", 456);
		String actual = new ObjectMapper().writeValueAsString(joined);
		assertThat(actual).isEqualToIgnoringWhitespace(expected);
		assertThat(new ObjectMapper().registerModule(new ParameterNamesModule()).readValue(actual, GameChatEvent.class))
			.isEqualTo(joined);
	}
}