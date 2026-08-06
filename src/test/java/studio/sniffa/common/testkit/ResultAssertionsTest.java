package studio.sniffa.common.testkit;

import org.junit.jupiter.api.Test;
import studio.sniffa.common.result.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static studio.sniffa.common.testkit.ResultAssertions.assertErr;
import static studio.sniffa.common.testkit.ResultAssertions.assertOk;

class ResultAssertionsTest {

    @Test
    void assertOkReturnsTheValue() {
        assertEquals("value", assertOk(Result.ok("value")));
    }

    @Test
    void assertOkThrowsOnErr() {
        assertThrows(AssertionError.class, () -> assertOk(Result.err("boom")));
    }

    @Test
    void assertErrReturnsTheError() {
        assertEquals("boom", assertErr(Result.err("boom")));
    }

    @Test
    void assertErrThrowsOnOk() {
        assertThrows(AssertionError.class, () -> assertErr(Result.ok("value")));
    }
}
