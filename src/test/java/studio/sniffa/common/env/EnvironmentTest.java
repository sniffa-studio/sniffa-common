package studio.sniffa.common.env;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentTest {

    @Test
    void defaultsToDevWhenUnset() {
        assertEquals(Environment.DEV, Environment.detect(key -> null));
    }

    @Test
    void readsAKnownValueCaseInsensitively() {
        assertEquals(Environment.PROD, Environment.detect(key -> "prod"));
        assertEquals(Environment.STAGING, Environment.detect(key -> "STAGING"));
    }

    @Test
    void throwsOnAnUnknownValue() {
        assertThrows(IllegalStateException.class, () -> Environment.detect(key -> "not-a-real-env"));
    }
}
