package studio.sniffa.common.shutdown;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Registers cleanup callbacks to run in order on shutdown, as a single, explicit sequence instead
 * of several independent {@code Runtime.addShutdownHook} calls scattered across a service (which
 * run in an arbitrary, JVM-chosen order). One failing hook doesn't stop the rest from running.
 */
public final class ShutdownHooks {

    private record Hook(String name, Runnable action) {
    }

    private final List<Hook> hooks = new ArrayList<>();
    private final BiConsumer<String, Exception> onError;

    public ShutdownHooks() {
        this((name, e) -> { });
    }

    public ShutdownHooks(BiConsumer<String, Exception> onError) {
        this.onError = onError;
    }

    /** Registers a cleanup action, run in registration order when {@link #runAll()} fires. */
    public ShutdownHooks register(String name, Runnable action) {
        hooks.add(new Hook(name, action));
        return this;
    }

    /** Installs a single JVM shutdown hook that runs every registered action, in order, on JVM exit. */
    public void install() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::runAll, "shutdown-hooks"));
    }

    /** Runs every registered action now, in order - for tests or explicit shutdown paths that don't want to wait for JVM exit. */
    public void runAll() {
        for (Hook hook : hooks) {
            try {
                hook.action().run();
            } catch (Exception e) {
                onError.accept(hook.name(), e);
            }
        }
    }
}
