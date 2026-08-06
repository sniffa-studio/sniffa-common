package studio.sniffa.common.resilience.policy;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.resilience.CircuitBreaker;
import studio.sniffa.common.resilience.CircuitOpenException;
import studio.sniffa.common.resilience.RetryPolicy;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResiliencePolicyTest {

    @Test
    void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        ResiliencePolicy policy = ResiliencePolicy.builder()
                .withRetry(new RetryPolicy(3, Duration.ofMillis(1), Duration.ofMillis(5), 1.0))
                .build();

        String result = policy.call(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void circuitBreakerOpensAcrossRetries() {
        AtomicInteger attempts = new AtomicInteger();
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofSeconds(5));
        ResiliencePolicy policy = ResiliencePolicy.builder()
                .withCircuitBreaker(breaker)
                .withRetry(new RetryPolicy(5, Duration.ofMillis(1), Duration.ofMillis(5), 1.0))
                .build();

        assertThrows(Exception.class, () -> policy.call(() -> {
            attempts.incrementAndGet();
            throw new RuntimeException("always fails");
        }));

        // breaker opens after the first failure (threshold 1) - later retry attempts fail fast
        // with CircuitOpenException instead of calling the action again.
        assertThrows(CircuitOpenException.class, () -> policy.call(() -> {
            attempts.incrementAndGet();
            return "unreachable";
        }));
    }
}
