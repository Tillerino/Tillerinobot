package org.tillerino.ppaddict.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Response sent to the user as the result of a command
 */
public interface GameChatResponse extends Iterable<GameChatResponse> {
	/**
	 * A regular IRC message. This should not be used as the direct response to
	 * a command, but for other auxiliary messages, see {@link Success}.
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class Message extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * A regular IRC message, which will be logged as a successfully executed command.
	 * This is the message that the command duration will be logged for.
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class Success extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * An "action" type IRC message
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	public static class Action extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * Adds another response to the current one.
	 */
	default GameChatResponse then(@CheckForNull GameChatResponse nextResponse) {
		if (nextResponse instanceof NoResponse || nextResponse == null) {
			return this;
		}
		if (this instanceof NoResponse) {
			return nextResponse;
		}
		ResponseList list = new ResponseList();
		if (this instanceof ResponseList) {
			list.responses.addAll(((ResponseList) this).responses);
		} else {
			list.responses.add(this);
		}
		if (nextResponse instanceof ResponseList) {
			list.responses.addAll(((ResponseList) nextResponse).responses);
		} else {
			list.responses.add(nextResponse);
		}
		return list;
	}

	static NoResponse none() {
		return new NoResponse();
	}

	default boolean isNone() {
		return this instanceof NoResponse;
	}

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
		public Iterator<GameChatResponse> iterator() {
			return Collections.emptyIterator();
		}
	}

	@EqualsAndHashCode
	@ToString
	@SuppressFBWarnings(value = "RCN", justification = "Generated code")
	public static final class ResponseList implements GameChatResponse {
		private final List<GameChatResponse> responses = new ArrayList<>();

		@Override
		public Iterator<GameChatResponse> iterator() {
			return responses.iterator();
		}
	}

	abstract class SingletonResponse implements GameChatResponse {
		@Override
		public Iterator<GameChatResponse> iterator() {
			return Arrays.asList((GameChatResponse) this).iterator();
		}
	}
}