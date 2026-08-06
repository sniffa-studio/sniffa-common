package studio.sniffa.common.discord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakesTest {

    @Test
    void parsesValidSnowflake() {
        assertEquals(1533929651758698557L, Snowflakes.parse("1533929651758698557"));
    }

    @Test
    void trimsWhitespace() {
        assertEquals(123L, Snowflakes.parse("  123  "));
    }

    @Test
    void throwsOnNull() {
        assertThrows(NumberFormatException.class, () -> Snowflakes.parse(null));
    }

    @Test
    void throwsOnBlank() {
        assertThrows(NumberFormatException.class, () -> Snowflakes.parse("   "));
    }

    @Test
    void throwsOnNonNumeric() {
        assertThrows(NumberFormatException.class, () -> Snowflakes.parse("not-a-snowflake"));
    }

    @Test
    void isValidReflectsParseability() {
        assertTrue(Snowflakes.isValid("123456789"));
        assertFalse(Snowflakes.isValid("nope"));
        assertFalse(Snowflakes.isValid(null));
    }
}
