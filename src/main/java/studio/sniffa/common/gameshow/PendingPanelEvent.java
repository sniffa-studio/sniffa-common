package studio.sniffa.common.gameshow;

/** An event that has no Discord join-button panel posted yet - created via sniffa-dashboard, not the bot's own {@code /gameshow-panel} command. */
public record PendingPanelEvent(String eventId, String title, String description, long guildId, long channelId) {
}
