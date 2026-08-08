package studio.sniffa.common.gameshow;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Transport-agnostic view of sniffa-backend's gameshow API — implemented by {@link GameshowApiClient}
 * (REST) and {@code GrpcGameshowClient} (gRPC). Callers (e.g. the Discord bot's GameshowListener)
 * depend on this instead of a concrete transport, so switching transports is a config change, not
 * a code change.
 */
public interface GameshowClient {

    EventInfo createEvent(String title, String description, long guildId, long channelId)
            throws IOException, InterruptedException;

    Optional<EventInfo> findActiveEvent(long channelId) throws IOException, InterruptedException;

    /** Same lookup, but by guild instead of channel - see {@code /gameshow-draw-test}. */
    Optional<EventInfo> findActiveEventInGuild(long guildId) throws IOException, InterruptedException;

    EntryOutcome submitEntry(String eventId, long discordUserId, String discordTag,
                              String ignUsername, String discordNameTyped) throws IOException, InterruptedException;

    default List<Winner> draw(String eventId, int count) throws IOException, InterruptedException {
        return draw(eventId, count, false);
    }

    /**
     * {@code dryRun=true} samples winners the same way but doesn't mark entries drawn or remove
     * them from the pool - lets staff test the draw/announcement flow against real entries without
     * it counting for the actual event.
     */
    default List<Winner> draw(String eventId, int count, boolean dryRun) throws IOException, InterruptedException {
        return draw(eventId, count, dryRun, null, null);
    }

    /**
     * @param guaranteedDiscordUserId if not null, this entrant is placed at guaranteedPosition
     *                                (1-based) instead of being randomly sampled - e.g. a content
     *                                creator staff wants at a specific spot. Must be given together
     *                                with guaranteedPosition, and that entrant must already have an
     *                                undrawn entry for the event. The other slots are still random.
     */
    List<Winner> draw(String eventId, int count, boolean dryRun, Long guaranteedDiscordUserId, Integer guaranteedPosition)
            throws IOException, InterruptedException;

    List<Entry> listEntries(String eventId) throws IOException, InterruptedException;

    /** Events (created via sniffa-dashboard) that have no Discord panel posted yet. */
    List<PendingPanelEvent> listPendingPanelEvents() throws IOException, InterruptedException;

    /** Called after actually posting an event's panel, so it isn't picked up again by {@link #listPendingPanelEvents}. */
    void markPanelPosted(String eventId) throws IOException, InterruptedException;
}
