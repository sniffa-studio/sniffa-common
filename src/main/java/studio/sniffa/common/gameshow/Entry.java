package studio.sniffa.common.gameshow;

public record Entry(String id, long discordUserId, String discordTag, String ignUsername,
                     String discordNameTyped, boolean drawn) {
}
