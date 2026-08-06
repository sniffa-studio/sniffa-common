package studio.sniffa.common.testkit;

import studio.sniffa.common.result.Result;

/**
 * Assertion helpers for {@link Result}, framework-free (plain {@link AssertionError}, no JUnit
 * dependency forced onto every consumer) so any test framework can use them.
 */
public final class ResultAssertions {

    private ResultAssertions() {
    }

    @SuppressWarnings("unchecked")
    public static <T, E> T assertOk(Result<T, E> result) {
        if (result instanceof Result.Err<T, E> err) {
            throw new AssertionError("Expected Ok, got Err: " + err.error());
        }
        return ((Result.Ok<T, E>) result).value();
    }

    public static <T, E> E assertErr(Result<T, E> result) {
        if (result instanceof Result.Ok<T, E> ok) {
            throw new AssertionError("Expected Err, got Ok: " + ok.value());
        }
        return ((Result.Err<T, E>) result).error();
    }
}
