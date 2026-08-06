package studio.sniffa.common.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenGeneratorTest {

    @Test
    void generatesDistinctTokens() {
        assertNotEquals(TokenGenerator.generate(), TokenGenerator.generate());
    }

    @Test
    void defaultTokenIsUrlSafe() {
        String token = TokenGenerator.generate();
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
    }

    @Test
    void customByteLengthChangesEncodedLength() {
        String token16 = TokenGenerator.generate(16);
        String token32 = TokenGenerator.generate(32);
        assertTrue(token16.length() < token32.length());
    }

    @Test
    void rejectsTooShortLength() {
        assertThrows(IllegalArgumentException.class, () -> TokenGenerator.generate(8));
    }

    @Test
    void defaultUsesThirtyTwoBytes() {
        // base64url without padding: ceil(32 * 8 / 6) = 43 chars
        assertEquals(43, TokenGenerator.generate().length());
    }
}
