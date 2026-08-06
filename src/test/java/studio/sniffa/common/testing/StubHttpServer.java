package studio.sniffa.common.testing;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * A tiny in-process HTTP server for testing HTTP clients, wrapping the JDK-builtin
 * {@code com.sun.net.httpserver.HttpServer} - no mocking framework needed. Extracted from what
 * used to be near-identical setup/teardown code in {@code SniffaHttpClientTest} and
 * {@code GameshowApiClientTest}.
 */
public final class StubHttpServer implements AutoCloseable {

    private final HttpServer server;

    private StubHttpServer(HttpServer server) {
        this.server = server;
    }

    public static StubHttpServer start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        return new StubHttpServer(server);
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    /** For canned responses that don't need to inspect the request. */
    public void respond(String path, int status, String body) {
        handle(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });
    }

    /** For tests that need to inspect the request (headers, body) before responding. */
    public void handle(String path, HttpHandler handler) {
        server.createContext(path, handler);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
