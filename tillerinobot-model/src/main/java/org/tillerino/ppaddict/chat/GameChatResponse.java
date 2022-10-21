package org.tillerino.ppaddict.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 * Response sent to the user as the result of a command
 */
@JsonTypeInfo(use = Id.MINIMAL_CLASS)
public interface GameChatResponse {
	/**
	 * A regular IRC message. This should not be used as the direct response to
	 * a command, but for other auxiliary messages, see {@link Success}.
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
	public static class Message extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * A regular IRC message, which will be logged as a successfully executed command.
	 * This is the message that the command duration will be logged for.
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
	public static class Success extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * An "action" type IRC message
	 */
	@Value
	@EqualsAndHashCode(callSuper = false)
	@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
	public static class Action extends GameChatResponse.SingletonResponse {
		String content;
	}

	/**
	 * Adds another response to the current one.
	 */
	@SuppressFBWarnings("SA_LOCAL_SELF_COMPARISON")
	default GameChatResponse then(@CheckForNull GameChatResponse nextResponse) {
		if (nextResponse instanceof NoResponse || nextResponse == null) {
			return this;
		}
		if (this instanceof NoResponse) {
			return nextResponse;
		}
		ResponseList list = new ResponseList();
		if (this instanceof ResponseList thisList) {
			list.responses.addAll(thisList.responses);
		} else {
			list.responses.add(this);
		}
		if (nextResponse instanceof ResponseList nextList) {
			list.responses.addAll(nextList.responses);
		} else {
			list.responses.add(nextResponse);
		}
		return list;
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

	@EqualsAndHashCode
	@ToString
	@SuppressFBWarnings(value = "RCN", justification = "Generated code")
	@AllArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__(@JsonCreator))
	public static final class ResponseList implements GameChatResponse {
		@Getter
		private final List<GameChatResponse> responses = new ArrayList<>();

		@Override
		public Iterable<GameChatResponse> flatten() {
			return Collections.unmodifiableList(responses);
		}
	}

	abstract class SingletonResponse implements GameChatResponse {
		@Override
		public Iterable<GameChatResponse> flatten() {
			return Collections.singletonList(this);
		}
	}
}