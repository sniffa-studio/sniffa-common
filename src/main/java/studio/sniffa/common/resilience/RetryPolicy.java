package studio.sniffa.common.resilience;

import java.time.Duration;

/** How {@link Retry#execute} should back off between attempts. */
public record RetryPolicy(int maxAttempts, Duration initialDelay, Duration maxDelay, double backoffMultiplier) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (backoffMultiplier < 1.0) {
            throw new IllegalArgumentException("backoffMultiplier must be at least 1.0");
        }
    }

    /** 3 attempts, 200ms initial delay doubling up to a 5s cap - a reasonable default for a flaky HTTP/gRPC call. */
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, Duration.ofMillis(200), Duration.ofSeconds(5), 2.0);
    }
}
