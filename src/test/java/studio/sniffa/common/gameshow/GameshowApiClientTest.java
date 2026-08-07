package studio.sniffa.common.gameshow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.sniffa.common.http.SniffaHttpClient;
import studio.sniffa.common.testing.StubHttpServer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameshowApiClientTest {

    private StubHttpServer server;
    private GameshowApiClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = StubHttpServer.start();
        SniffaHttpClient http = new SniffaHttpClient(server.baseUrl(), "token");
        client = new GameshowApiClient(http);
    }

    @AfterEach
    void stopServer() {
        server.close();
    }

    @Test
    void createEventParsesResponse() throws Exception {
        server.respond("/api/v1/events", 200, """
                {"eventId":"evt1","title":"Show","description":"desc"}""");

        EventInfo event = client.createEvent("Show", "desc", 1, 2);

        assertEquals("evt1", event.eventId());
        assertEquals("Show", event.title());
    }

    @Test
    void findActiveEventReturnsEmptyOn404() throws Exception {
        server.respond("/api/v1/events/active", 404, "");

        Optional<EventInfo> event = client.findActiveEvent(42);

        assertTrue(event.isEmpty());
    }

    @Test
    void submitEntryReturnsAlreadyJoinedOn409() throws Exception {
        server.respond("/api/v1/events/evt1/entries", 409, "");

        EntryOutcome outcome = client.submitEntry("evt1", 1, "tag#1", "ign", "name");

        assertInstanceOf(EntryOutcome.AlreadyJoined.class, outcome);
    }

    @Test
    void drawReturnsWinners() throws Exception {
        server.respond("/api/v1/events/evt1/draw", 200, """
                {"winners":[{"discordUserId":123,"discordTag":"tag#1","ignUsername":"ign"}]}""");

        List<Winner> winners = client.draw("evt1", 1);

        assertEquals(1, winners.size());
        assertEquals(123L, winners.get(0).discordUserId());
    }

    @Test
    void listEntriesParsesDrawnStatus() throws Exception {
        server.respond("/api/v1/events/evt1/entries", 200, """
                {"entries":[{"id":"e1","discordUserId":1,"discordTag":"t","ignUsername":"i","discordNameTyped":"d","drawn":true}]}""");

        List<Entry> entries = client.listEntries("evt1");

        assertEquals(1, entries.size());
        assertTrue(entries.get(0).drawn());
    }

    @Test
    void listEntriesHandlesEmptyList() throws Exception {
        server.respond("/api/v1/events/evt1/entries", 200, """
                {"entries":[]}""");

        assertTrue(client.listEntries("evt1").isEmpty());
    }

    @Test
    void listPendingPanelEventsParsesResponse() throws Exception {
        server.respond("/api/v1/events/pending-panel", 200, """
                {"events":[{"eventId":"evt1","title":"Show","description":"desc","guildId":1,"channelId":2}]}""");

        List<PendingPanelEvent> events = client.listPendingPanelEvents();

        assertEquals(1, events.size());
        assertEquals("evt1", events.get(0).eventId());
        assertEquals(2L, events.get(0).channelId());
    }

    @Test
    void markPanelPostedSendsRequest() throws Exception {
        server.respond("/api/v1/events/evt1/panel-posted", 204, "");

        client.markPanelPosted("evt1");
    }
}
