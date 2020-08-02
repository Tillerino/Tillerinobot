package org.tillerino.ppaddict.rabbit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.tillerino.ppaddict.chat.GameChatResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteResponseQueueTest {
	private static final ObjectMapper OBJECT_MAPPER = RabbitMqConfiguration.mapper();

	@Test
	public void testSerializations() throws Exception {
		roundTrip(new GameChatResponse.Success("abc"));
		roundTrip(new GameChatResponse.Message("abc"));
		roundTrip(new GameChatResponse.Action("abc"));
		roundTrip(new GameChatResponse.Action("abc").then(new GameChatResponse.Success("def")));
		roundTrip(GameChatResponse.none());
	}

	private void roundTrip(GameChatResponse message) throws JsonProcessingException, JsonMappingException {
		String serialized = OBJECT_MAPPER.writerFor(GameChatResponse.class).writeValueAsString(message);
		GameChatResponse deserialized = OBJECT_MAPPER.readValue(serialized, GameChatResponse.class);
		assertThat(deserialized).isEqualTo(message);
	}

}
