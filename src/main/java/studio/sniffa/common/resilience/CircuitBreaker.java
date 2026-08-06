package studio.sniffa.common.resilience;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple circuit breaker: after {@code failureThreshold} consecutive failures it opens and fails
 * fast with {@link CircuitOpenException} for {@code resetTimeout}, then allows one trial call
 * (half-open) - a success closes it again, a failure re-opens it for another full timeout.
 */
public final class CircuitBreaker {

    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final Duration resetTimeout;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private volatile Instant openedAt = Instant.EPOCH;

    public CircuitBreaker(int failureThreshold, Duration resetTimeout) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold must be at least 1");
        }
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
    }

    public <T> T call(Callable<T> action) throws Exception {
        if (state.get() == State.OPEN) {
            if (Instant.now().isBefore(openedAt.plus(resetTimeout))) {
                throw new CircuitOpenException("Circuit is open until " + openedAt.plus(resetTimeout));
            }
            state.set(State.HALF_OPEN);
        }
        try {
            T result = action.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    public boolean isOpen() {
        return state.get() == State.OPEN;
    }

    private void onSuccess() {
        consecutiveFailures.set(0);
        state.set(State.CLOSED);
    }

    private void onFailure() {
        if (state.get() == State.HALF_OPEN || consecutiveFailures.incrementAndGet() >= failureThreshold) {
            state.set(State.OPEN);
            openedAt = Instant.now();
        }
    }
}
