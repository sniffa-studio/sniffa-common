package studio.sniffa.common.validation;

import java.util.List;
import java.util.stream.Collectors;

/** Thrown by {@link Validator#requireValid(Object)} when one or more rules fail. */
public final class ValidationException extends RuntimeException {

    private final List<Violation> violations;

    public ValidationException(List<Violation> violations) {
        super(violations.stream()
                .map(v -> v.field() + ": " + v.message())
                .collect(Collectors.joining("; ")));
        this.violations = List.copyOf(violations);
    }

    public List<Violation> violations() {
        return violations;
    }
}
