package studio.sniffa.common.resilience;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryTest {

    private static final RetryPolicy FAST_POLICY = new RetryPolicy(3, Duration.ofMillis(1), Duration.ofMillis(5), 2.0);

    @Test
    void returnsImmediatelyOnFirstSuccess() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retry.execute(FAST_POLICY, () -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilSuccess() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = Retry.execute(FAST_POLICY, () -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("transient");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsAfterExhaustingAttempts() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(RuntimeException.class, () -> Retry.execute(FAST_POLICY, () -> {
            calls.incrementAndGet();
            throw new RuntimeException("always fails");
        }));
        assertEquals(FAST_POLICY.maxAttempts(), calls.get());
    }

    @Test
    void nonRetryableFailureStopsImmediately() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(IllegalStateException.class, () -> Retry.execute(FAST_POLICY, e -> false, () -> {
            calls.incrementAndGet();
            throw new IllegalStateException("not worth retrying");
        }));
        assertEquals(1, calls.get());
    }

    @Test
    void interruptedExceptionIsNotRetried() {
        AtomicInteger calls = new AtomicInteger();
        try {
            assertThrows(InterruptedException.class, () -> Retry.execute(FAST_POLICY, () -> {
                calls.incrementAndGet();
                throw new InterruptedException();
            }));
            assertEquals(1, calls.get());
        } finally {
            // Retry.execute restores the interrupt flag on the calling thread - clear it so it
            // doesn't leak into later tests sharing this thread.
            Thread.interrupted();
        }
    }
}
