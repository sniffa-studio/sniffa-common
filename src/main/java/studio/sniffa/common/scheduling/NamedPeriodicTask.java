package studio.sniffa.common.scheduling;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Runs a named task on a fixed interval, catching and reporting per-tick failures instead of
 * letting one bad tick kill the whole schedule - the "poll every N seconds, don't die on
 * exception, stop cleanly on shutdown" shape that sniffa-node-agent's Reconciler and
 * sniffa-proxy's ServerRegistry.sync() each currently hand-roll separately.
 *
 * <p>Deliberately framework-free (no SLF4J dependency forced on every consumer, same reasoning as
 * {@code logging.CorrelationContext}) - failures go to the supplied {@code onError} callback,
 * which each caller wires into its own logging.
 */
public final class NamedPeriodicTask implements AutoCloseable {

    private final String name;
    private final ScheduledExecutorService executor;

    private NamedPeriodicTask(String name, ScheduledExecutorService executor) {
        this.name = name;
        this.executor = executor;
    }

    public static NamedPeriodicTask start(String name, Duration interval, Runnable task, Consumer<Throwable> onError) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                onError.accept(e);
            }
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        return new NamedPeriodicTask(name, executor);
    }

    public String name() {
        return name;
    }

    /** Stops the schedule, waiting briefly for an in-flight tick to finish before forcing a shutdown. */
    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
