package org.tillerino.ppaddict.chat;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.tillerino.ppaddict.rabbit.Rpc;
import org.tillerino.ppaddict.util.Result;

/** Writes a response to an event to the game chat. */
public interface GameChatWriter {
    /**
     * Responds with a direct message.
     *
     * @param response the message to send
     * @param recipient the recipient of the message
     */
    @Rpc(queue = "irc_writer", timeout = 12000)
    Result<Response, Error> message(String response, @IRCName String recipient);

    /**
     * Responds with an "action", a special kind of direct message.
     *
     * @param response the action to send
     * @param recipient the recipient of the action
     */
    @Rpc(queue = "irc_writer", timeout = 12000)
    Result<Response, Error> action(String response, @IRCName String recipient);

    @JsonTypeInfo(use = Id.MINIMAL_CLASS)
    sealed interface Error {
        record Timeout() implements Error {}

        record Unknown() implements Error {}

        record Retry(int millis) implements Error {}

        record PingDeath(long millis) implements Error {}
    }

    record Response(Long ping) {}
}
