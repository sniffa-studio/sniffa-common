package studio.sniffa.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A small nested-JSON document with dot-path access ({@code "a.b.c"}), for services that need more
 * structure than {@code ConfigLoader}'s flat properties+env-var model. Mirrors what
 * marguhl-gameshow's Gson-based {@code JsonFile} already does for that plugin's own config files -
 * this is the Jackson-based equivalent, available for future services on this stack.
 */
public final class JsonDocument {

    private final ObjectNode root;

    private JsonDocument(ObjectNode root) {
        this.root = root;
    }

    public static JsonDocument empty() {
        return new JsonDocument(Json.mapper().createObjectNode());
    }

    public static JsonDocument load(Path file) {
        try {
            JsonNode node = Json.mapper().readTree(file.toFile());
            if (!(node instanceof ObjectNode object)) {
                throw new IllegalArgumentException(file + " does not contain a JSON object at its root");
            }
            return new JsonDocument(object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<String> getString(String dotPath) {
        JsonNode node = at(dotPath);
        return node != null && node.isTextual() ? Optional.of(node.asText()) : Optional.empty();
    }

    public OptionalLong getLong(String dotPath) {
        JsonNode node = at(dotPath);
        return node != null && node.isNumber() ? OptionalLong.of(node.asLong()) : OptionalLong.empty();
    }

    public Optional<JsonDocument> getSection(String dotPath) {
        JsonNode node = at(dotPath);
        return node instanceof ObjectNode object ? Optional.of(new JsonDocument(object)) : Optional.empty();
    }

    public JsonDocument set(String dotPath, Object value) {
        String[] segments = dotPath.split("\\.");
        ObjectNode target = root;
        for (int i = 0; i < segments.length - 1; i++) {
            JsonNode next = target.get(segments[i]);
            ObjectNode child = next instanceof ObjectNode existing ? existing : target.putObject(segments[i]);
            target = child;
        }
        // valueToTree (not putPOJO) so a String/long/boolean lands as a proper JSON text/number/
        // boolean node - putPOJO would wrap it as an opaque POJONode that getString/getLong can't see.
        target.set(segments[segments.length - 1], Json.mapper().valueToTree(value));
        return this;
    }

    /** Writes atomically: full serialize to a temp file, then a single {@code Files.move}. */
    public void save(Path file) {
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            if (file.toAbsolutePath().getParent() != null) {
                Files.createDirectories(file.toAbsolutePath().getParent());
            }
            Json.mapper().writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), root);
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JsonNode at(String dotPath) {
        JsonNode current = root;
        for (String segment : dotPath.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }
}
