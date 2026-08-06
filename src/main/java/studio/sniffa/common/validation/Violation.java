package studio.sniffa.common.validation;

/** A single failed validation rule, identifying the field/component and why it failed. */
public record Violation(String field, String message) {
}
