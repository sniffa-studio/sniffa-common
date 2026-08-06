package studio.sniffa.common.resilience.policy;

import studio.sniffa.common.resilience.CircuitBreaker;
import studio.sniffa.common.resilience.Retry;
import studio.sniffa.common.resilience.RetryPolicy;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Chains an existing {@link CircuitBreaker} and {@link RetryPolicy} into one callable action, so
 * a call site doesn't have to hand-nest "retry wraps circuit breaker" itself. Composition only -
 * {@code CircuitBreaker}/{@code Retry} stay independent and usable on their own; this just wires
 * them together for the common case of wanting both around the same call (e.g. an agent's gRPC
 * call to the backend, or an HTTP client call).
 */
public final class ResiliencePolicy {

    private final CircuitBreaker circuitBreaker;
    private final RetryPolicy retryPolicy;
    private final Predicate<Exception> retryable;

    private ResiliencePolicy(CircuitBreaker circuitBreaker, RetryPolicy retryPolicy, Predicate<Exception> retryable) {
        this.circuitBreaker = circuitBreaker;
        this.retryPolicy = retryPolicy;
        this.retryable = retryable;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Runs {@code action} through the configured circuit breaker (if any) wrapped in the configured retry (if any). */
    public <T> T call(Callable<T> action) throws Exception {
        Callable<T> guarded = circuitBreaker == null ? action : () -> circuitBreaker.call(action);
        if (retryPolicy == null) {
            return guarded.call();
        }
        return Retry.execute(retryPolicy, retryable, guarded);
    }

    public static final class Builder {
        private CircuitBreaker circuitBreaker;
        private RetryPolicy retryPolicy;
        private Predicate<Exception> retryable = e -> true;

        private Builder() {
        }

        public Builder withCircuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }

        public Builder withRetry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        /** Decides whether a given failure is worth retrying (e.g. only network errors, not 4xx responses). Defaults to retrying everything. */
        public Builder retryable(Predicate<Exception> retryable) {
            this.retryable = retryable;
            return this;
        }

        public ResiliencePolicy build() {
            return new ResiliencePolicy(circuitBreaker, retryPolicy, retryable);
        }
    }
}
