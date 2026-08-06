package studio.sniffa.common.json;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class JsonTest {

    record Ping(String name, Instant createdAt) {
    }

    @Test
    void roundTripsInstant() {
        Instant now = Instant.parse("2026-08-06T12:00:00Z");
        Ping ping = new Ping("test", now);

        String json = Json.write(ping);
        Ping parsed = Json.read(json, Ping.class);

        assertEquals(ping, parsed);
    }

    @Test
    void ignoresUnknownProperties() {
        Ping parsed = Json.read("{\"name\":\"test\",\"createdAt\":\"2026-08-06T12:00:00Z\",\"extra\":true}", Ping.class);
        assertEquals("test", parsed.name());
    }

    @Test
    void mapperIsSharedInstance() {
        assertSame(Json.mapper(), Json.mapper());
    }
}
