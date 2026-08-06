package studio.sniffa.common.config;

import java.util.List;

/**
 * Thrown by {@link ConfigLoader#requireAll} listing every missing/invalid key at once, instead of
 * failing on the first one and forcing a fix-restart-fix cycle for each subsequent key.
 */
public final class ConfigValidationException extends RuntimeException {

    private final List<String> problems;

    public ConfigValidationException(List<String> problems) {
        super("Invalid configuration:\n" + String.join("\n", problems));
        this.problems = List.copyOf(problems);
    }

    public List<String> problems() {
        return problems;
    }
}
