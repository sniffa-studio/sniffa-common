package studio.sniffa.common.cache;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.testkit.FakeClock;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InMemoryCacheTest {

    @Test
    void returnsAPutValueBeforeItExpires() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryCache<String, String> cache = new InMemoryCache<>(Duration.ofMinutes(5), clock);

        cache.put("key", "value");

        assertEquals("value", cache.get("key"));
    }

    @Test
    void returnsNullAfterTtlExpires() {
        FakeClock clock = FakeClock.at(Instant.parse("2026-01-01T00:00:00Z"));
        InMemoryCache<String, String> cache = new InMemoryCache<>(Duration.ofMinutes(5), clock);

        cache.put("key", "value");
        clock.advance(Duration.ofMinutes(6));

        assertNull(cache.get("key"));
    }

    @Test
    void invalidateRemovesTheEntry() {
        InMemoryCache<String, String> cache = new InMemoryCache<>(Duration.ofMinutes(5));

        cache.put("key", "value");
        cache.invalidate("key");

        assertNull(cache.get("key"));
    }

    @Test
    void missingKeyReturnsNull() {
        InMemoryCache<String, String> cache = new InMemoryCache<>(Duration.ofMinutes(5));

        assertNull(cache.get("missing"));
    }
}
