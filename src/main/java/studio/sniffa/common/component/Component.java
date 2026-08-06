package studio.sniffa.common.component;

/**
 * A named, independently startable/stoppable piece of a service - the shape already used
 * informally by sniffa-backend's {@code BackendModule} (name + register). Kept here as a small,
 * generic contract so other services (sniffa-node-agent, sniffa-proxy) can adopt the same mental
 * model for "how do I add a piece of behavior" instead of each inventing its own.
 *
 * <p>Not yet adopted by {@code BackendModule} itself - migrating an already-deployed module
 * pattern is separate, lower-risk work from introducing the shape here first.
 */
public interface Component {

    String name();

    default void start() {
    }

    default void stop() {
    }
}
