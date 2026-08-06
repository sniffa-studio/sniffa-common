package studio.sniffa.common.resilience;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Retries a fallible action with exponential backoff. None of {@code SniffaHttpClient},
 * {@code GrpcGameshowClient}, or marguhl-gameshow's backend poller retry a transient failure today
 * - they either propagate it or (the poller) just wait for the next scheduled poll. This exists so
 * a future caller can opt into backoff-and-retry without hand-rolling a loop per call site.
 */
public final class Retry {

    private Retry() {
    }

    public static <T> T execute(RetryPolicy policy, Callable<T> action) throws Exception {
        return execute(policy, e -> true, action);
    }

    /** @param retryable decides whether a given failure is worth retrying (e.g. only network errors, not 4xx responses). */
    public static <T> T execute(RetryPolicy policy, Predicate<Exception> retryable, Callable<T> action) throws Exception {
        Duration delay = policy.initialDelay();
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return action.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                if (attempt == policy.maxAttempts() || !retryable.test(e)) {
                    throw e;
                }
                Thread.sleep(delay.toMillis());
                delay = Duration.ofMillis(Math.min((long) (delay.toMillis() * policy.backoffMultiplier()), policy.maxDelay().toMillis()));
            }
        }
        throw new IllegalStateException("unreachable");
    }
}
