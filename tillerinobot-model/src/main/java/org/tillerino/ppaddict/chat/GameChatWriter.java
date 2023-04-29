package org.tillerino.ppaddict.chat;

import java.io.IOException;
import java.util.Optional;

import org.tillerino.ppaddict.rabbit.Rpc;
import org.tillerino.ppaddict.util.Result;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Writes a response to an event to the game chat.
 */
public interface GameChatWriter {
	/**
	 * Responds with a direct message.
	 *
	 * @param response the message to send
	 * @param recipient the recipient of the message
	 */
	@Rpc(queue = "irc_writer", timeout = 12000)
	Result<Response, Error> message(String response, @IRCName String recipient) throws InterruptedException, IOException;

	/**
	 * Responds with an "action", a special kind of direct message.
	 *
	 * @param response the action to send
	 * @param recipient the recipient of the action
	 */
	@Rpc(queue = "irc_writer", timeout = 12000)
	Result<Response, Error> action(String response, @IRCName String recipient) throws InterruptedException, IOException;

	@JsonTypeInfo(use = Id.MINIMAL_CLASS)
	sealed interface Error {
		public record Timeout() implements Error { }

		public record Unknown() implements Error { }

		public record Retry(int millis) implements Error { }

		public record PingDeath(long millis) implements Error { }
	}

	public record Response(Long ping) { }
}
