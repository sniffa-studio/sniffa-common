package studio.sniffa.common.time;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpiringTest {

    private static Clock fixedClockAt(String isoInstant) {
        return Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC);
    }

    @Test
    void notExpiredBeforeTtl() {
        Clock issuedAt = fixedClockAt("2026-01-01T00:00:00Z");
        Expiring<String> session = Expiring.of("token", Duration.ofMinutes(30), issuedAt);

        Clock oneMinuteLater = fixedClockAt("2026-01-01T00:01:00Z");
        assertFalse(session.isExpired(oneMinuteLater));
    }

    @Test
    void expiredAfterTtl() {
        Clock issuedAt = fixedClockAt("2026-01-01T00:00:00Z");
        Expiring<String> session = Expiring.of("token", Duration.ofMinutes(30), issuedAt);

        Clock thirtyOneMinutesLater = fixedClockAt("2026-01-01T00:31:00Z");
        assertTrue(session.isExpired(thirtyOneMinutesLater));
    }

    @Test
    void expiresAtExactBoundary() {
        Clock issuedAt = fixedClockAt("2026-01-01T00:00:00Z");
        Expiring<String> session = Expiring.of("token", Duration.ofMinutes(30), issuedAt);

        Clock exactlyAtExpiry = fixedClockAt("2026-01-01T00:30:00Z");
        assertTrue(session.isExpired(exactlyAtExpiry));
    }
}
