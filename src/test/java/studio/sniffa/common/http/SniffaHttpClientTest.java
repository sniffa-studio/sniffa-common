package studio.sniffa.common.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.sniffa.common.testing.StubHttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SniffaHttpClientTest {

    private StubHttpServer server;
    private SniffaHttpClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = StubHttpServer.start();
        client = new SniffaHttpClient(server.baseUrl(), "test-token");
    }

    @AfterEach
    void stopServer() {
        server.close();
    }

    @Test
    void getSendsBearerTokenAndReturnsBody() throws Exception {
        AtomicReference<String> receivedAuth = new AtomicReference<>();
        server.handle("/ping", exchange -> {
            receivedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = "pong".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        HttpResponse<String> response = client.get("/ping");

        assertEquals(200, response.statusCode());
        assertEquals("pong", response.body());
        assertEquals("Bearer test-token", receivedAuth.get());
    }

    @Test
    void getSendsCorrelationIdHeader() throws Exception {
        AtomicReference<String> receivedCorrelationId = new AtomicReference<>();
        server.handle("/ping", exchange -> {
            receivedCorrelationId.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            exchange.sendResponseHeaders(200, -1);
        });

        client.get("/ping");

        assertTrue(receivedCorrelationId.get() != null && !receivedCorrelationId.get().isBlank());
    }

    @Test
    void postSendsJsonBody() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server.handle("/echo", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
        });

        client.post("/echo", client.newObject().put("hello", "world"));

        assertTrue(receivedBody.get().contains("\"hello\":\"world\""));
    }

    @Test
    void ensureSuccessThrowsOnNon2xx() throws Exception {
        server.respond("/fail", 500, "");

        HttpResponse<String> response = client.get("/fail");

        assertThrows(IOException.class, () -> SniffaHttpClient.ensureSuccess(response));
    }
}
