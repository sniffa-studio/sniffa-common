package studio.sniffa.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureCompareTest {

    @Test
    void equalStringsMatch() {
        assertTrue(SecureCompare.equals("secret-token", "secret-token"));
    }

    @Test
    void differentValuesDoNotMatch() {
        assertFalse(SecureCompare.equals("secret-token", "wrong-token"));
    }

    @Test
    void differentLengthsDoNotMatch() {
        assertFalse(SecureCompare.equals("short", "much-longer-value"));
    }

    @Test
    void emptyStringsMatch() {
        assertTrue(SecureCompare.equals("", ""));
    }
}
