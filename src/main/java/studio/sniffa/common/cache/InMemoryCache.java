package studio.sniffa.common.cache;

import studio.sniffa.common.time.Expiring;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A TTL-based, in-memory-only {@link Cache} - no LRU eviction, no size bound, deliberately simple.
 * Reuses {@code time.Expiring} for the TTL bookkeeping instead of reinventing it. Entries are only
 * pruned lazily on {@link #get} - fine for the small, low-cardinality lookups this is meant for
 * (config values, Docker image metadata), not a general-purpose cache for unbounded keyspaces.
 */
public final class InMemoryCache<K, V> implements Cache<K, V> {

    private final Map<K, Expiring<V>> entries = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    public InMemoryCache(Duration ttl) {
        this(ttl, Clock.systemUTC());
    }

    public InMemoryCache(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public V get(K key) {
        Expiring<V> entry = entries.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(clock)) {
            entries.remove(key, entry);
            return null;
        }
        return entry.value();
    }

    @Override
    public void put(K key, V value) {
        entries.put(key, Expiring.of(value, ttl, clock));
    }

    @Override
    public void invalidate(K key) {
        entries.remove(key);
    }
}
