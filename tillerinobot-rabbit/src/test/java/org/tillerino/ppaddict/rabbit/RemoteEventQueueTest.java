package org.tillerino.ppaddict.rabbit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.Sighted;
import org.tillerino.ppaddict.util.MdcUtils;
import org.tillerino.ppaddict.util.MdcUtils.MdcAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteEventQueueTest {
	private static final ObjectMapper OBJECT_MAPPER = RabbitMqConfiguration.mapper();

	@Test
	public void testSerializations() throws Exception {
		roundTrip(new PrivateMessage(123, "n", 456, "m"));
		roundTrip(new PrivateAction(123, "n", 456, "a"));
		roundTrip(new Sighted(123, "n", 456));
		roundTrip(new Joined(123, "n", 456));
	}

	private void roundTrip(GameChatEvent message) throws JsonProcessingException, JsonMappingException {
		try (MdcAttributes with = MdcUtils.with("mdck", "mdcv")) {
			message.getMeta().setMdc(MdcUtils.getSnapshot());
			message.getMeta().setRateLimiterBlockedTime(234);
			String serialized = OBJECT_MAPPER.writerFor(GameChatEvent.class).writeValueAsString(message);
			GameChatEvent deserialized = OBJECT_MAPPER.readValue(serialized, GameChatEvent.class);
			assertThat(deserialized).isEqualTo(message);
		}
	}
}
