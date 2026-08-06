package studio.sniffa.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultTest {

    @Test
    void okReportsOkNotErr() {
        Result<Integer, String> result = Result.ok(1);
        assertTrue(result.isOk());
        assertFalse(result.isErr());
    }

    @Test
    void errReportsErrNotOk() {
        Result<Integer, String> result = Result.err("failed");
        assertTrue(result.isErr());
        assertFalse(result.isOk());
    }

    @Test
    void mapTransformsOkValue() {
        Result<Integer, String> result = Result.<Integer, String>ok(2).map(v -> v * 10);
        assertEquals(20, result.orElse(-1));
    }

    @Test
    void mapLeavesErrUntouched() {
        Result<Integer, String> result = Result.<Integer, String>err("nope").map(v -> v * 10);
        assertTrue(result.isErr());
        assertEquals(-1, result.orElse(-1));
    }

    @Test
    void flatMapChainsOkResults() {
        Result<Integer, String> result = Result.<Integer, String>ok(2)
                .flatMap(v -> Result.ok(v + 1))
                .flatMap(v -> Result.ok(v * 10));
        assertEquals(30, result.orElse(-1));
    }

    @Test
    void flatMapShortCircuitsOnErr() {
        Result<Integer, String> result = Result.<Integer, String>err("nope")
                .flatMap(v -> Result.ok(v + 1));
        assertTrue(result.isErr());
    }

    @Test
    void orElseThrowReturnsValueForOk() {
        Result<Integer, String> result = Result.ok(5);
        assertEquals(5, result.orElseThrow(RuntimeException::new));
    }

    @Test
    void orElseThrowThrowsForErr() {
        Result<Integer, String> result = Result.err("boom");
        RuntimeException e = assertThrows(RuntimeException.class,
                () -> result.orElseThrow(IllegalStateException::new));
        assertEquals("boom", e.getMessage());
    }

    @Test
    void okAndErrHaveValueEquality() {
        assertEquals(Result.ok("a"), Result.ok("a"));
        assertEquals(Result.err("x"), Result.err("x"));
    }
}
