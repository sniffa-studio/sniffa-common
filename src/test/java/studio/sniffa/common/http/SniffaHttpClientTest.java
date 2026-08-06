package studio.sniffa.common.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SniffaHttpClientTest {

    private HttpServer server;
    private SniffaHttpClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        client = new SniffaHttpClient("http://127.0.0.1:" + server.getAddress().getPort(), "test-token");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void getSendsBearerTokenAndReturnsBody() throws Exception {
        AtomicReference<String> receivedAuth = new AtomicReference<>();
        server.createContext("/ping", exchange -> {
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
    void postSendsJsonBody() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server.createContext("/echo", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
        });

        client.post("/echo", client.newObject().put("hello", "world"));

        assertTrue(receivedBody.get().contains("\"hello\":\"world\""));
    }

    @Test
    void ensureSuccessThrowsOnNon2xx() throws Exception {
        server.createContext("/fail", exchange -> {
            exchange.sendResponseHeaders(500, -1);
        });

        HttpResponse<String> response = client.get("/fail");

        assertThrows(IOException.class, () -> SniffaHttpClient.ensureSuccess(response));
    }
}
