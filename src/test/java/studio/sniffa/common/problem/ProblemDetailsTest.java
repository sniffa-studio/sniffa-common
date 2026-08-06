package studio.sniffa.common.problem;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.validation.ValidationException;
import studio.sniffa.common.validation.Violation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProblemDetailsTest {

    @Test
    void ofBuildsAnEmptyErrorList() {
        ProblemDetails problem = ProblemDetails.of("about:blank", "Not Found", 404, "no such resource");

        assertEquals(404, problem.status());
        assertEquals(List.of(), problem.errors());
    }

    @Test
    void fromValidationMapsEveryViolation() {
        ValidationException exception = new ValidationException(List.of(
                new Violation("name", "must not be blank"),
                new Violation("age", "must be at least 0")
        ));

        ProblemDetails problem = ProblemDetails.fromValidation(exception);

        assertEquals(400, problem.status());
        assertEquals(List.of("name: must not be blank", "age: must be at least 0"), problem.errors());
    }
}
