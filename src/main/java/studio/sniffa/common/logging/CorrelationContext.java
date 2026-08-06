package studio.sniffa.common.logging;

import studio.sniffa.common.id.TimeOrderedId;

/**
 * A thread-local correlation ID for tracing one logical request across the bot -> backend ->
 * plugin chain. Deliberately dependency-free (no SLF4J/MDC forced on every consumer) - each
 * service reads {@link #current()} and puts it into its own logging context however it already
 * does logging. {@code SniffaHttpClient} attaches it to outgoing requests as {@code X-Correlation-Id}.
 */
public final class CorrelationContext {

    private static final String HEADER = "X-Correlation-Id";
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CorrelationContext() {
    }

    public static String headerName() {
        return HEADER;
    }

    /** Returns the current correlation ID, generating and storing a new one if none is set yet. */
    public static String current() {
        String id = CURRENT.get();
        if (id == null) {
            id = TimeOrderedId.generate();
            CURRENT.set(id);
        }
        return id;
    }

    /** Adopts an ID received from elsewhere (e.g. an inbound request header) instead of generating one. */
    public static void set(String id) {
        CURRENT.set(id);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
