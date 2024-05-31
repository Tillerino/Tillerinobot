package org.tillerino.ppaddict.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;

/**
 * Response sent to the user as the result of a command
 */
@JsonTypeInfo(use = Id.MINIMAL_CLASS)
public interface GameChatResponse {
	/**
	 * A regular IRC message. This should not be used as the direct response to
	 * a command, but for other auxiliary messages, see {@link Success}.
	 */
	public record Message(String content) implements GameChatResponse.SingletonResponse {
	}

	/**
	 * A regular IRC message, which will be logged as a successfully executed command.
	 * This is the message that the command duration will be logged for.
	 */
	public record Success(String content) implements GameChatResponse.SingletonResponse {
	}

	/**
	 * An "action" type IRC message
	 */
	public record Action(String content) implements GameChatResponse.SingletonResponse {
	}

	/**
	 * Adds another response to the current one.
	 */
	@SuppressFBWarnings("SA_LOCAL_SELF_COMPARISON")
	default GameChatResponse then(@CheckForNull GameChatResponse that) {
		if (that instanceof NoResponse || that == null) {
			return this;
		}
		if (this instanceof NoResponse) {
			return that;
		}
		List<GameChatResponse> responses = new ArrayList<>();
		if (this instanceof ResponseList(var these)) {
			responses.addAll(these);
		} else {
			responses.add(this);
		}
		if (that instanceof ResponseList(var those)) {
			responses.addAll(those);
		} else {
			responses.add(that);
		}
		return new ResponseList(responses);
	}

	static NoResponse none() {
		return new NoResponse();
	}

	@JsonIgnore
	default boolean isNone() {
		return this instanceof NoResponse;
	}

	@JsonIgnore
	Iterable<GameChatResponse> flatten();

	/**
	 * Returned by the handler to clarify that the command was handled, but no
	 * response is sent.
	 */
	@EqualsAndHashCode
	public static final class NoResponse implements GameChatResponse {
		@Override
		public String toString() {
			return "[No Response]";
		}

		@Override
		public Iterable<GameChatResponse> flatten() {
			return Collections.emptyList();
		}
	}

	@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record bro")
	public record ResponseList(List<GameChatResponse> responses) implements GameChatResponse {
		@Override
		public Iterable<GameChatResponse> flatten() {
			return Collections.unmodifiableList(responses);
		}
	}

	interface SingletonResponse extends GameChatResponse {
		@Override
		default Iterable<GameChatResponse> flatten() {
			return Collections.singletonList(this);
		}
	}
}