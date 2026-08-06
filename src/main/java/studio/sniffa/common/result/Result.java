package studio.sniffa.common.result;

import java.util.function.Function;

/**
 * A generic success/failure result, for services that don't need a domain-specific outcome type
 * (see e.g. {@code EntryOutcome} in the {@code gameshow} package) but still want to model failure
 * as data instead of a thrown exception.
 */
public sealed interface Result<T, E> {

    boolean isOk();

    boolean isErr();

    <U> Result<U, E> map(Function<T, U> mapper);

    <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper);

    T orElse(T fallback);

    T orElseThrow(Function<E, ? extends RuntimeException> exceptionMapper);

    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    record Ok<T, E>(T value) implements Result<T, E> {

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public <U> Result<U, E> map(Function<T, U> mapper) {
            return new Ok<>(mapper.apply(value));
        }

        @Override
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public T orElse(T fallback) {
            return value;
        }

        @Override
        public T orElseThrow(Function<E, ? extends RuntimeException> exceptionMapper) {
            return value;
        }
    }

    record Err<T, E>(E error) implements Result<T, E> {

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> map(Function<T, U> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        public T orElse(T fallback) {
            return fallback;
        }

        @Override
        public T orElseThrow(Function<E, ? extends RuntimeException> exceptionMapper) {
            throw exceptionMapper.apply(error);
        }
    }
}
