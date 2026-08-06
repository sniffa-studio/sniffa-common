package studio.sniffa.common.scheduling;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedPeriodicTaskTest {

    @Test
    void runsRepeatedlyUntilClosed() throws InterruptedException {
        CountDownLatch ranThreeTimes = new CountDownLatch(3);

        NamedPeriodicTask task = NamedPeriodicTask.start("test-task", Duration.ofMillis(10),
                ranThreeTimes::countDown, e -> { });
        try {
            assertTrue(ranThreeTimes.await(2, TimeUnit.SECONDS));
        } finally {
            task.close();
        }
    }

    @Test
    void aFailingTickDoesNotStopSubsequentTicks() throws InterruptedException {
        AtomicInteger tickCount = new AtomicInteger();
        CountDownLatch failedThreeTimes = new CountDownLatch(3);

        NamedPeriodicTask task = NamedPeriodicTask.start("failing-task", Duration.ofMillis(10), () -> {
            tickCount.incrementAndGet();
            throw new RuntimeException("boom");
        }, e -> failedThreeTimes.countDown());

        try {
            assertTrue(failedThreeTimes.await(2, TimeUnit.SECONDS));
            assertTrue(tickCount.get() >= 3);
        } finally {
            task.close();
        }
    }
}
