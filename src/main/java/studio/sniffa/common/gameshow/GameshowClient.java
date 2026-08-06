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

    EntryOutcome submitEntry(String eventId, long discordUserId, String discordTag,
                              String ignUsername, String discordNameTyped) throws IOException, InterruptedException;

    List<Winner> draw(String eventId, int count) throws IOException, InterruptedException;

    List<Entry> listEntries(String eventId) throws IOException, InterruptedException;
}
