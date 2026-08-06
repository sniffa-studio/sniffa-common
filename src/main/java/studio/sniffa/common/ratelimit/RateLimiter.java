package studio.sniffa.common.ratelimit;

/**
 * Decides whether a call identified by {@code key} (an IP, a user ID, ...) is allowed right now.
 * {@code InMemoryRateLimiter} is the only implementation - the interface exists so call sites
 * (a Javalin filter, a gRPC interceptor) don't depend on the specific algorithm.
 */
public interface RateLimiter {

    boolean tryAcquire(String key);
}
