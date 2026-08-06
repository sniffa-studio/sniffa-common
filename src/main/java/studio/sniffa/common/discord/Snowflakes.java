package studio.sniffa.common.discord;

/**
 * Parsing/validation for Discord snowflake IDs (guild, channel, user, message IDs, ...) — 64-bit
 * numbers that exceed JavaScript's safe integer range, which is why they're carried as strings on
 * any JSON boundary a browser touches (see sniffa-backend's dashboard API) but parsed back to a
 * plain {@code long} everywhere else.
 */
public final class Snowflakes {

    private Snowflakes() {
    }

    /** @throws NumberFormatException if {@code value} is null, blank, or not a valid snowflake. */
    public static long parse(String value) {
        if (value == null || value.isBlank()) {
            throw new NumberFormatException("snowflake value is missing");
        }
        return Long.parseLong(value.trim());
    }

    public static boolean isValid(String value) {
        try {
            parse(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
