package studio.sniffa.common.id;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * A time-ordered, lexicographically sortable ID: a 48-bit millisecond timestamp followed by 80
 * bits of randomness, Crockford Base32 encoded (26 characters) - similar in spirit to a ULID.
 * Unlike {@link studio.sniffa.common.security.TokenGenerator}'s pure-random secrets, two IDs
 * generated further apart in time sort in that same order as plain strings.
 */
public final class TimeOrderedId {

    // Crockford's Base32 alphabet - omits I, L, O, U to avoid confusion with 1/1/0/V.
    private static final char[] ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final int ENCODED_LENGTH = 26;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TimeOrderedId() {
    }

    public static String generate() {
        return generate(Instant.now());
    }

    public static String generate(Instant instant) {
        byte[] bytes = new byte[16];

        long millis = instant.toEpochMilli();
        for (int i = 0; i < 6; i++) {
            bytes[5 - i] = (byte) (millis >>> (8 * i));
        }

        byte[] randomness = new byte[10];
        RANDOM.nextBytes(randomness);
        System.arraycopy(randomness, 0, bytes, 6, 10);

        return encode(bytes);
    }

    private static String encode(byte[] bytes) {
        BigInteger value = new BigInteger(1, bytes);
        BigInteger base = BigInteger.valueOf(32);
        char[] out = new char[ENCODED_LENGTH];
        for (int i = ENCODED_LENGTH - 1; i >= 0; i--) {
            BigInteger[] divRem = value.divideAndRemainder(base);
            out[i] = ALPHABET[divRem[1].intValue()];
            value = divRem[0];
        }
        return new String(out);
    }
}
