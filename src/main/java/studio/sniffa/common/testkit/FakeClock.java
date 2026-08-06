package studio.sniffa.common.testkit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A settable {@link Clock} for testing time-based code ({@code time.Expiring}, resilience
 * timeouts, ...) without sleeping in tests. Framework-free (no JUnit/Mockito dependency), so
 * unlike {@code src/test/.../testing.StubHttpServer} (internal to this repo's own tests), this
 * lives in the main sourceSet and is reusable by every consumer repo's own tests too.
 */
public final class FakeClock extends Clock {

    private Instant now;
    private final ZoneId zone;

    private FakeClock(Instant now, ZoneId zone) {
        this.now = now;
        this.zone = zone;
    }

    public static FakeClock at(Instant now) {
        return new FakeClock(now, ZoneId.of("UTC"));
    }

    public void advance(Duration duration) {
        now = now.plus(duration);
    }

    public void set(Instant instant) {
        now = instant;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new FakeClock(now, zone);
    }

    @Override
    public Instant instant() {
        return now;
    }
}
