package studio.sniffa.common.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reflective validation against the annotations in this package. Reads a target's record
 * components first (records are the standard DTO shape across this codebase) and falls back to
 * declared fields for a plain class.
 */
public final class Validator {

    private Validator() {
    }

    public static List<Violation> validate(Object target) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        List<Violation> violations = new ArrayList<>();
        RecordComponent[] components = target.getClass().getRecordComponents();
        if (components != null) {
            for (RecordComponent component : components) {
                check(component.getName(), component.getAnnotations(), read(component, target), violations);
            }
        } else {
            for (Field field : target.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                check(field.getName(), field.getAnnotations(), read(field, target), violations);
            }
        }
        return violations;
    }

    /** @throws ValidationException if {@link #validate(Object)} finds any rule violated. */
    public static void requireValid(Object target) {
        List<Violation> violations = validate(target);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
    }

    private static void check(String name, Annotation[] annotations, Object value, List<Violation> violations) {
        for (Annotation annotation : annotations) {
            switch (annotation) {
                case NotNull ignored -> {
                    if (value == null) {
                        violations.add(new Violation(name, "must not be null"));
                    }
                }
                case NotBlank ignored -> {
                    if (!(value instanceof String s) || s.isBlank()) {
                        violations.add(new Violation(name, "must not be blank"));
                    }
                }
                case Min min -> {
                    if (value instanceof Number n && n.longValue() < min.value()) {
                        violations.add(new Violation(name, "must be at least " + min.value()));
                    }
                }
                case Max max -> {
                    if (value instanceof Number n && n.longValue() > max.value()) {
                        violations.add(new Violation(name, "must be at most " + max.value()));
                    }
                }
                case Size size -> {
                    int length = sizeOf(value);
                    if (length >= 0 && (length < size.min() || length > size.max())) {
                        violations.add(new Violation(name, "size must be between " + size.min() + " and " + size.max()));
                    }
                }
                default -> {
                }
            }
        }
    }

    private static int sizeOf(Object value) {
        if (value instanceof String s) {
            return s.length();
        }
        if (value instanceof Collection<?> c) {
            return c.size();
        }
        return -1;
    }

    private static Object read(RecordComponent component, Object target) {
        try {
            return component.getAccessor().invoke(target);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not read record component " + component.getName(), e);
        }
    }

    private static Object read(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not read field " + field.getName(), e);
        }
    }
}
