package studio.sniffa.common.testkit;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.time.Expiring;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeClockTest {

    @Test
    void advanceMovesTimeForward() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        Expiring<String> value = Expiring.of("token", Duration.ofMinutes(5), clock);

        assertFalse(value.isExpired(clock));

        clock.advance(Duration.ofMinutes(6));

        assertTrue(value.isExpired(clock));
    }

    @Test
    void setJumpsToAnExactInstant() {
        FakeClock clock = FakeClock.at(Instant.EPOCH);
        Instant target = Instant.parse("2030-01-01T00:00:00Z");

        clock.set(target);

        assertTrue(clock.instant().equals(target));
    }
}
