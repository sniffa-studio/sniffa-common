package studio.sniffa.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin, reusable HTTP transport for talking to a sniffa-backend-style REST API: a base URL, a
 * bearer token, and JSON bodies via Jackson. Domain-specific clients (e.g. {@code GameshowApiClient})
 * build their typed methods on top of this instead of repeating HTTP/JSON plumbing per project.
 */
public final class SniffaHttpClient {

    private final String baseUrl;
    private final String bearerToken;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SniffaHttpClient(String baseUrl, String bearerToken) {
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public ObjectNode newObject() {
        return mapper.createObjectNode();
    }

    public HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return send(baseRequest(path).GET().build());
    }

    public HttpResponse<String> post(String path, ObjectNode body) throws IOException, InterruptedException {
        return send(baseRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build());
    }

    public HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        return send(baseRequest(path).DELETE().build());
    }

    public <T> T readValue(HttpResponse<String> response, Class<T> type) throws IOException {
        return mapper.readValue(response.body(), type);
    }

    public static void ensureSuccess(HttpResponse<String> response) throws IOException {
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Request failed: " + response.statusCode() + " " + response.body());
        }
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + bearerToken)
                .timeout(Duration.ofSeconds(10));
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
