package studio.sniffa.common.cache;

/**
 * A minimal key-value cache contract - {@code InMemoryCache} is the only implementation for now,
 * but the interface lets a Redis-backed one (part of the separate, still-conceptual sniffa-data
 * library, see tech-stack.md) drop in later without call sites changing.
 */
public interface Cache<K, V> {

    V get(K key);

    void put(K key, V value);

    void invalidate(K key);
}
