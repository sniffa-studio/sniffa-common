package studio.sniffa.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigLoaderTest {

    @Test
    void requireReadsValueFromPropertiesFile(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "TOKEN=abc123");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertEquals("abc123", config.require("TOKEN"));
    }

    @Test
    void requireThrowsWhenKeyMissingEverywhere(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertThrows(IllegalStateException.class, () -> config.require("MISSING_KEY"));
    }

    @Test
    void requireThrowsOnBlankValue(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "TOKEN=   ");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertThrows(IllegalStateException.class, () -> config.require("TOKEN"));
    }

    @Test
    void optionalFallsBackWhenMissing(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertEquals("default", config.optional("MISSING_KEY", "default"));
    }

    @Test
    void requireLongParsesNumericValue(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "CHANNEL_ID=123456789012345678");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertEquals(123456789012345678L, config.requireLong("CHANNEL_ID"));
    }

    @Test
    void requireLongThrowsOnNonNumericValue(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "CHANNEL_ID=not-a-number");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertThrows(IllegalStateException.class, () -> config.requireLong("CHANNEL_ID"));
    }

    @Test
    void optionalLongReturnsNullWhenMissing(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertNull(config.optionalLong("MISSING_KEY"));
    }

    @Test
    void optionalIntFallsBackWhenMissing(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertEquals(8091, config.optionalInt("PORT", 8091));
    }

    @Test
    void requireAllReturnsAllValuesWhenPresent(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "A=1\nB=2");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        assertEquals(java.util.Map.of("A", "1", "B", "2"), config.requireAll("A", "B"));
    }

    @Test
    void requireAllCollectsEveryMissingKeyInOneException(@TempDir Path tempDir) throws IOException {
        Path file = writeProperties(tempDir, "PRESENT=ok");
        ConfigLoader config = ConfigLoader.fromFile(file.toString());

        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
                () -> config.requireAll("PRESENT", "MISSING_A", "MISSING_B"));

        assertEquals(2, exception.problems().size());
    }

    @Test
    void missingPropertiesFileDoesNotThrow(@TempDir Path tempDir) {
        ConfigLoader config = ConfigLoader.fromFile(tempDir.resolve("does-not-exist.properties").toString());

        assertEquals("default", config.optional("ANY_KEY", "default"));
    }

    private static Path writeProperties(Path dir, String content) throws IOException {
        Path file = dir.resolve("config.properties");
        Files.writeString(file, content);
        return file;
    }
}
