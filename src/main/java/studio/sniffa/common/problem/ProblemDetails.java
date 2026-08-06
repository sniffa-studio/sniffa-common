package studio.sniffa.common.problem;

import java.util.List;

/**
 * A structured error response following <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC
 * 7807</a>, so every HTTP route across the ecosystem returns errors in the same shape instead of
 * an ad-hoc string message the Dashboard has to guess the format of.
 *
 * <p>Deliberately framework-free - no Javalin dependency here (sniffa-common stays free of
 * backend-specific dependencies, see tech-stack.md's sniffa-data rationale). Wiring this into
 * Javalin's exception handling is the consumer's job (sniffa-backend), this is just the shared
 * data shape plus the {@link #fromValidation} mapping.
 */
public record ProblemDetails(String type, String title, int status, String detail, List<String> errors) {

    public static ProblemDetails of(String type, String title, int status, String detail) {
        return new ProblemDetails(type, title, status, detail, List.of());
    }

    /** Maps {@code validation.ValidationException}'s violations into one problem's error list. */
    public static ProblemDetails fromValidation(studio.sniffa.common.validation.ValidationException exception) {
        List<String> errors = exception.violations().stream()
                .map(v -> v.field() + ": " + v.message())
                .toList();
        return new ProblemDetails("about:blank", "Validation Failed", 400, exception.getMessage(), errors);
    }
}
