package studio.sniffa.common.security;

/** Constant-time string comparison so auth checks (tokens, passwords) don't leak timing info. */
public final class SecureCompare {

    private SecureCompare() {
    }

    public static boolean equals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
