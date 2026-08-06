package studio.sniffa.common.resilience;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitBreakerTest {

    @Test
    void staysClosedOnSuccess() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(50));
        assertEquals("ok", breaker.call(() -> "ok"));
        assertFalse(breaker.isOpen());
    }

    @Test
    void opensAfterConsecutiveFailures() {
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(50));
        failOnce(breaker);
        assertFalse(breaker.isOpen());
        failOnce(breaker);
        assertTrue(breaker.isOpen());
    }

    @Test
    void failsFastWhileOpen() {
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofSeconds(5));
        failOnce(breaker);
        assertTrue(breaker.isOpen());

        assertThrows(CircuitOpenException.class, () -> breaker.call(() -> "should not run"));
    }

    @Test
    void successAfterResetTimeoutClosesAgain() throws Exception {
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(20));
        failOnce(breaker);
        assertTrue(breaker.isOpen());

        Thread.sleep(30);

        assertEquals("ok", breaker.call(() -> "ok"));
        assertFalse(breaker.isOpen());
    }

    @Test
    void failureDuringHalfOpenReopensCircuit() {
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofMillis(20));
        failOnce(breaker);
        assertTrue(breaker.isOpen());

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThrows(RuntimeException.class, () -> breaker.call(() -> {
            throw new RuntimeException("still failing");
        }));
        assertTrue(breaker.isOpen());
    }

    private void failOnce(CircuitBreaker breaker) {
        assertThrows(RuntimeException.class, () -> breaker.call(() -> {
            throw new RuntimeException("boom");
        }));
    }
}
