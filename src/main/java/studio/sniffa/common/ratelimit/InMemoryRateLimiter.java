package studio.sniffa.common.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A per-key token bucket: {@code capacity} tokens available, refilling continuously over
 * {@code refillPeriod} (e.g. 10 tokens per minute). No external dependency, no distributed state -
 * each process enforces its own limit, which is enough for the single-node deployments this
 * ecosystem runs today.
 */
public final class InMemoryRateLimiter implements RateLimiter {

    private static final class Bucket {
        double tokens;
        Instant lastRefill;

        Bucket(double tokens, Instant lastRefill) {
            this.tokens = tokens;
            this.lastRefill = lastRefill;
        }
    }

    private final int capacity;
    private final Duration refillPeriod;
    private final Clock clock;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(int capacity, Duration refillPeriod) {
        this(capacity, refillPeriod, Clock.systemUTC());
    }

    public InMemoryRateLimiter(int capacity, Duration refillPeriod, Clock clock) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, clock.instant()));
        synchronized (bucket) {
            refill(bucket);
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private void refill(Bucket bucket) {
        Instant now = clock.instant();
        double elapsedMillis = Duration.between(bucket.lastRefill, now).toMillis();
        double refilled = elapsedMillis / refillPeriod.toMillis() * capacity;
        if (refilled > 0) {
            bucket.tokens = Math.min(capacity, bucket.tokens + refilled);
            bucket.lastRefill = now;
        }
    }
}
