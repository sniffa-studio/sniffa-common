package studio.sniffa.common.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    record Signup(
            @NotBlank String name,
            @NotNull String email,
            @Min(1) @Max(10) int slotCount,
            @Size(min = 1, max = 3) List<String> tags
    ) {
    }

    @Test
    void validRecordHasNoViolations() {
        Signup signup = new Signup("Marguhl", "a@b.com", 5, List.of("vip"));
        assertTrue(Validator.validate(signup).isEmpty());
    }

    @Test
    void blankStringFailsNotBlank() {
        Signup signup = new Signup(" ", "a@b.com", 5, List.of("vip"));
        List<Violation> violations = Validator.validate(signup);
        assertEquals(1, violations.size());
        assertEquals("name", violations.get(0).field());
    }

    @Test
    void nullFailsNotNull() {
        Signup signup = new Signup("Marguhl", null, 5, List.of("vip"));
        List<Violation> violations = Validator.validate(signup);
        assertEquals(1, violations.size());
        assertEquals("email", violations.get(0).field());
    }

    @Test
    void outOfRangeFailsMinAndMax() {
        Signup tooLow = new Signup("Marguhl", "a@b.com", 0, List.of("vip"));
        assertEquals(1, Validator.validate(tooLow).size());

        Signup tooHigh = new Signup("Marguhl", "a@b.com", 11, List.of("vip"));
        assertEquals(1, Validator.validate(tooHigh).size());
    }

    @Test
    void emptyCollectionFailsSize() {
        Signup signup = new Signup("Marguhl", "a@b.com", 5, List.of());
        List<Violation> violations = Validator.validate(signup);
        assertEquals(1, violations.size());
        assertEquals("tags", violations.get(0).field());
    }

    @Test
    void multipleViolationsAreAllReported() {
        Signup signup = new Signup("", null, 0, List.of());
        assertEquals(4, Validator.validate(signup).size());
    }

    @Test
    void requireValidThrowsWithAllViolations() {
        Signup signup = new Signup("", null, 0, List.of());
        ValidationException exception = assertThrows(ValidationException.class, () -> Validator.requireValid(signup));
        assertEquals(4, exception.violations().size());
    }

    @Test
    void requireValidPassesSilently() {
        Signup signup = new Signup("Marguhl", "a@b.com", 5, List.of("vip"));
        Validator.requireValid(signup);
    }
}
