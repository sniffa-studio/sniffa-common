package studio.sniffa.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Loads config from a local properties file (if present) with environment variables as a
 * fallback for every key — lets contributors run a service locally via a properties file while
 * production/Docker deployments rely on env vars only. Shared by every Sniffa Studio service's
 * own typed config class (e.g. the Discord bot's {@code BotConfig}, the backend's
 * {@code BackendConfig}) instead of each re-implementing this parsing.
 */
public final class ConfigLoader {

    private final Properties properties;

    private ConfigLoader(Properties properties) {
        this.properties = properties;
    }

    public static ConfigLoader fromFile(String path) {
        Properties properties = new Properties();
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            try (InputStream in = Files.newInputStream(filePath)) {
                properties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + path, e);
            }
        }
        return new ConfigLoader(properties);
    }

    public String require(String key) {
        String value = rawValue(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config value '" + key
                    + "'. Set it in the properties file or as an environment variable.");
        }
        return value;
    }

    /**
     * Like {@link #require}, but for several keys at once: collects every missing key into a
     * single {@link ConfigValidationException} instead of throwing on the first one - so a
     * misconfigured deployment reports everything wrong with it in one boot attempt, not one key
     * per restart.
     */
    public Map<String, String> requireAll(String... keys) {
        Map<String, String> values = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        for (String key : keys) {
            String value = rawValue(key);
            if (value == null || value.isBlank()) {
                problems.add("Missing required config value '" + key + "'");
            } else {
                values.put(key, value);
            }
        }
        if (!problems.isEmpty()) {
            throw new ConfigValidationException(problems);
        }
        return values;
    }

    public String optional(String key, String fallback) {
        String value = rawValue(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public long requireLong(String key) {
        return parseLong(key, require(key));
    }

    public Long optionalLong(String key) {
        String value = rawValue(key);
        return (value == null || value.isBlank()) ? null : parseLong(key, value);
    }

    public int optionalInt(String key, int fallback) {
        String value = rawValue(key);
        return (value == null || value.isBlank()) ? fallback : (int) parseLong(key, value);
    }

    private String rawValue(String key) {
        return properties.getProperty(key, System.getenv(key));
    }

    private static long parseLong(String key, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Config value '" + key + "' must be numeric, got: " + value);
        }
    }
}
