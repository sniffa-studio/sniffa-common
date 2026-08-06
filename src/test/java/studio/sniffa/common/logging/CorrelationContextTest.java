package studio.sniffa.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CorrelationContextTest {

    @AfterEach
    void clear() {
        CorrelationContext.clear();
    }

    @Test
    void generatesAndStableWithinThread() {
        String first = CorrelationContext.current();
        String second = CorrelationContext.current();
        assertEquals(first, second);
    }

    @Test
    void clearingProducesANewId() {
        String first = CorrelationContext.current();
        CorrelationContext.clear();
        String second = CorrelationContext.current();
        assertNotEquals(first, second);
    }

    @Test
    void setAdoptsAGivenId() {
        CorrelationContext.set("from-upstream-request");
        assertEquals("from-upstream-request", CorrelationContext.current());
    }

    @Test
    void headerNameIsStable() {
        assertEquals("X-Correlation-Id", CorrelationContext.headerName());
    }
}
