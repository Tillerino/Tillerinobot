package org.tillerino.ppaddict.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.NonNull;

/**
 * Inspired and kept as close as possible to Rust's Result type:
 * https://doc.rust-lang.org/src/core/result.rs.html
 *
 * Although this has some overhead, we use an interface with two implementations
 * since Record Patterns are right around the corner.
 *
 * This is very short for now, but we'll add more methods as we go.
 */
public sealed interface Result<T, E> {
	static <T, E> Ok<T, E> ok(@NonNull T t) {
		return new Ok<>(t);
	}

	static <T, E> Err<T, E> err(@NonNull E e) {
		return new Err<>(e);
	}

	default boolean isOk() {
		return this instanceof Ok;
	}

	default boolean isErr() {
		return this instanceof Err;
	}

	default Optional<T> ok() {
		if (this instanceof Ok<T, E> ok) {
			return Optional.of(ok.t);
		}
		return Optional.empty();
	}

	default Optional<E> err() {
		if (this instanceof Err<T, E> err) {
			return Optional.of(err.e);
		}
		return Optional.empty();
	}

	default Result<T, E> inspect(Consumer<T> f) {
		if (this instanceof Ok<T, E> ok) {
			f.accept(ok.t);
		}
		return this;
	}

	default T unwrapOrElse(Function<E, T> op) {
		if (this instanceof Ok<T, E> ok) {
			return ok.t;
		}
		else return op.apply(((Err<T, E>) this).e);
	}

	default T unwrapOr(T def) {
		if (this instanceof Ok<T, E> ok) {
			return ok.t;
		}
		else return def;
	}

	default <U> Result<U, E> map(Function<T, U> op) {
		if (this instanceof Ok<T, E> ok) {
			return ok(op.apply(ok.t));
		}
		else return (Result<U, E>) this;
	}

	@JsonValue
	default FlatResult<T, E> json() {
		return new FlatResult<>(ok().orElse(null), err().orElse(null));
	}

	@JsonCreator
	static <T, E> Result<T, E> fromJson(FlatResult<T, E> json) {
		if (json.ok() != null) {
			return ok(json.ok());
		}
		if (json.err() != null) {
			return err(json.err());
		}
		throw new IllegalArgumentException();
	}

	record Ok<T, E>(@NonNull T t) implements Result<T, E> {

	}

	record Err<T, E>(@NonNull E e) implements Result<T, E> {

	}

	@JsonInclude(value = Include.NON_NULL)
	record FlatResult<T, E>(T ok, E err) {

	}
}
