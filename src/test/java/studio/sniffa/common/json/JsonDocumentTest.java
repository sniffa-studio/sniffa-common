package studio.sniffa.common.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDocumentTest {

    @TempDir
    Path tempDir;

    @Test
    void setAndGetNestedString() {
        JsonDocument document = JsonDocument.empty().set("backend.host", "127.0.0.1");
        assertEquals("127.0.0.1", document.getString("backend.host").orElseThrow());
    }

    @Test
    void setAndGetNestedLong() {
        JsonDocument document = JsonDocument.empty().set("backend.pollIntervalSeconds", 15L);
        assertEquals(15L, document.getLong("backend.pollIntervalSeconds").orElseThrow());
    }

    @Test
    void missingPathIsEmpty() {
        JsonDocument document = JsonDocument.empty();
        assertTrue(document.getString("does.not.exist").isEmpty());
    }

    @Test
    void getSectionReturnsNestedDocument() {
        JsonDocument document = JsonDocument.empty().set("backend.host", "127.0.0.1");
        JsonDocument section = document.getSection("backend").orElseThrow();
        assertEquals("127.0.0.1", section.getString("host").orElseThrow());
    }

    @Test
    void wrongTypeIsEmptyNotThrown() {
        JsonDocument document = JsonDocument.empty().set("count", 5L);
        assertFalse(document.getString("count").isPresent());
    }

    @Test
    void saveAndLoadRoundTrips() {
        Path file = tempDir.resolve("config.json");
        JsonDocument.empty()
                .set("backend.host", "127.0.0.1")
                .set("backend.enabled", true)
                .save(file);

        JsonDocument loaded = JsonDocument.load(file);
        assertEquals("127.0.0.1", loaded.getString("backend.host").orElseThrow());
    }

    @Test
    void saveIsAtomicNoLeftoverTempFile() {
        Path file = tempDir.resolve("config.json");
        JsonDocument.empty().set("a", "b").save(file);
        assertFalse(tempDir.resolve("config.json.tmp").toFile().exists());
    }
}
