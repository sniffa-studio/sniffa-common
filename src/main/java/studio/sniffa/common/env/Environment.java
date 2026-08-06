package studio.sniffa.common.env;

import java.util.function.Function;

/**
 * Which environment a process is running in - a small, standalone concept deliberately kept out
 * of {@code config.ConfigLoader} (that's generic key/value loading, this is one specific,
 * ecosystem-wide question every service eventually needs to answer the same way).
 */
public enum Environment {
    DEV,
    STAGING,
    PROD;

    private static final String VARIABLE = "SNIFFA_ENV";

    /** Reads {@code SNIFFA_ENV} and defaults to {@link #DEV} if it's unset - a missing env var during local development shouldn't be an error. */
    public static Environment detect() {
        return detect(System::getenv);
    }

    /** Same as {@link #detect()} but with an injectable env-var lookup, for tests that don't want to touch real process state. */
    public static Environment detect(Function<String, String> envLookup) {
        String value = envLookup.apply(VARIABLE);
        if (value == null || value.isBlank()) {
            return DEV;
        }
        try {
            return Environment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown " + VARIABLE + " value: '" + value
                    + "'. Expected one of " + java.util.Arrays.toString(values()));
        }
    }
}
