package studio.sniffa.common.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A value with an expiry, for TTL-style data (session tokens, cached lookups, ...) so callers
 * don't each hand-roll their own "issuedAt + ttl" check - sniffa-backend's {@code SessionAuth}
 * currently does exactly that inline for its session cookie's TTL.
 */
public record Expiring<T>(T value, Instant expiresAt) {

    public static <T> Expiring<T> of(T value, Duration ttl, Clock clock) {
        return new Expiring<>(value, clock.instant().plus(ttl));
    }

    public static <T> Expiring<T> of(T value, Duration ttl) {
        return of(value, ttl, Clock.systemUTC());
    }

    public boolean isExpired(Clock clock) {
        return !clock.instant().isBefore(expiresAt);
    }

    public boolean isExpired() {
        return isExpired(Clock.systemUTC());
    }
}
