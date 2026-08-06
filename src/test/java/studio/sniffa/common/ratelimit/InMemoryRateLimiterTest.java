package studio.sniffa.common.ratelimit;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.testkit.FakeClock;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRateLimiterTest {

    @Test
    void allowsUpToCapacityThenDenies() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(2, Duration.ofSeconds(1), clock);

        assertTrue(limiter.tryAcquire("ip"));
        assertTrue(limiter.tryAcquire("ip"));
        assertFalse(limiter.tryAcquire("ip"));
    }

    @Test
    void refillsOverTime() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(1, Duration.ofSeconds(1), clock);

        assertTrue(limiter.tryAcquire("ip"));
        assertFalse(limiter.tryAcquire("ip"));

        clock.advance(Duration.ofSeconds(1));

        assertTrue(limiter.tryAcquire("ip"));
    }

    @Test
    void differentKeysHaveIndependentBuckets() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(1, Duration.ofSeconds(1), clock);

        assertTrue(limiter.tryAcquire("ip-a"));
        assertTrue(limiter.tryAcquire("ip-b"));
    }
}
