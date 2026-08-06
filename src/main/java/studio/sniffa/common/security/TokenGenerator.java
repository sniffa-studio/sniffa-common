package studio.sniffa.common.security;

import java.security.SecureRandom;
import java.util.Base64;

/** Generates URL-safe random secrets — API tokens, session secrets, admin passwords, ... */
public final class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_BYTE_LENGTH = 32;

    private TokenGenerator() {
    }

    /** A random token with {@code byteLength * 8} bits of entropy, URL-safe, unpadded. */
    public static String generate(int byteLength) {
        if (byteLength < 16) {
            throw new IllegalArgumentException("byteLength must be at least 16 for a usable secret");
        }
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 256 bits of entropy — the default already used for every Sniffa Studio API token/secret. */
    public static String generate() {
        return generate(DEFAULT_BYTE_LENGTH);
    }
}
