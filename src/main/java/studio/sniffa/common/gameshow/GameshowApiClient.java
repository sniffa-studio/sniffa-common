package studio.sniffa.common.gameshow;

import studio.sniffa.common.http.SniffaHttpClient;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * REST implementation of {@link GameshowClient} against sniffa-backend's {@code /api/events*}
 * bearer-authed API. Shared by sniffa-discord (creates events, submits entries, draws winners)
 * and any Minecraft plugin that needs to react to draws (e.g. marguhl-gameshow's whitelist sync).
 */
public final class GameshowApiClient implements GameshowClient {

    private final SniffaHttpClient http;

    public GameshowApiClient(SniffaHttpClient http) {
        this.http = http;
    }

    @Override
    public EventInfo createEvent(String title, String description, long guildId, long channelId)
            throws IOException, InterruptedException {
        var body = http.newObject()
                .put("title", title)
                .put("description", description)
                .put("guildId", guildId)
                .put("channelId", channelId);

        HttpResponse<String> response = http.post("/api/v1/events", body);
        SniffaHttpClient.ensureSuccess(response);
        return http.readValue(response, EventInfo.class);
    }

    @Override
    public Optional<EventInfo> findActiveEvent(long channelId) throws IOException, InterruptedException {
        HttpResponse<String> response = http.get("/api/v1/events/active?channelId=" + channelId);
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        SniffaHttpClient.ensureSuccess(response);
        return Optional.of(http.readValue(response, EventInfo.class));
    }

    @Override
    public Optional<EventInfo> findActiveEventInGuild(long guildId) throws IOException, InterruptedException {
        HttpResponse<String> response = http.get("/api/v1/events/active-in-guild?guildId=" + guildId);
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        SniffaHttpClient.ensureSuccess(response);
        return Optional.of(http.readValue(response, EventInfo.class));
    }

    @Override
    public EntryOutcome submitEntry(String eventId, long discordUserId, String discordTag,
                                     String ignUsername, String discordNameTyped) throws IOException, InterruptedException {
        var body = http.newObject()
                .put("discordUserId", discordUserId)
                .put("discordTag", discordTag)
                .put("ignUsername", ignUsername)
                .put("discordNameTyped", discordNameTyped);

        HttpResponse<String> response = http.post("/api/v1/events/" + eventId + "/entries", body);
        if (response.statusCode() == 409) {
            return new EntryOutcome.AlreadyJoined();
        }
        if (response.statusCode() / 100 != 2) {
            return new EntryOutcome.Error("backend returned " + response.statusCode());
        }
        return new EntryOutcome.Created();
    }

    @Override
    public List<Winner> draw(String eventId, int count, boolean dryRun, Long guaranteedDiscordUserId, Integer guaranteedPosition)
            throws IOException, InterruptedException {
        var body = http.newObject().put("count", count).put("dryRun", dryRun);
        if (guaranteedDiscordUserId != null) {
            body.put("guaranteedDiscordUserId", guaranteedDiscordUserId);
        }
        if (guaranteedPosition != null) {
            body.put("guaranteedPosition", guaranteedPosition);
        }
        HttpResponse<String> response = http.post("/api/v1/events/" + eventId + "/draw", body);
        SniffaHttpClient.ensureSuccess(response);
        return http.readValue(response, DrawResponse.class).winners();
    }

    /** Full entry list including {@code drawn} status — used to sync new winners into a plugin. */
    @Override
    public List<Entry> listEntries(String eventId) throws IOException, InterruptedException {
        HttpResponse<String> response = http.get("/api/v1/events/" + eventId + "/entries");
        SniffaHttpClient.ensureSuccess(response);
        return http.readValue(response, EntriesResponse.class).entries();
    }

    @Override
    public List<PendingPanelEvent> listPendingPanelEvents() throws IOException, InterruptedException {
        HttpResponse<String> response = http.get("/api/v1/events/pending-panel");
        SniffaHttpClient.ensureSuccess(response);
        return http.readValue(response, PendingPanelEventsResponse.class).events();
    }

    @Override
    public void markPanelPosted(String eventId) throws IOException, InterruptedException {
        HttpResponse<String> response = http.post("/api/v1/events/" + eventId + "/panel-posted", http.newObject());
        SniffaHttpClient.ensureSuccess(response);
    }

    private record DrawResponse(List<Winner> winners) {
    }

    private record EntriesResponse(List<Entry> entries) {
    }

    private record PendingPanelEventsResponse(List<PendingPanelEvent> events) {
    }
}
