package org.tillerino.ppaddict.chat;

import org.tillerino.ppaddict.rabbit.Rpc;
import org.tillerino.ppaddict.util.Result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * This is the "main function" of the chat bot, i.e. connects to the server,
 * starts accepting messages...
 */
public interface GameChatClient {
	@Rpc(queue = "game_chat_client", timeout = 1000)
	Result<GameChatClientMetrics, Error> getMetrics();

	@JsonTypeInfo(use = Id.MINIMAL_CLASS)
	sealed interface Error {
		public record Timeout() implements Error { }

		public record Unknown() implements Error { }
	}
}