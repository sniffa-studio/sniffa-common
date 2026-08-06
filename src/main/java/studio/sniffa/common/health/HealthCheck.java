package studio.sniffa.common.health;

/**
 * One thing a service can check about its own readiness (a database connection, a required
 * downstream dependency, ...). Implementations should be fast - health checks are meant to run on
 * demand (an HTTP /health hit, a pre-flight self-check), not in a hot path.
 */
public interface HealthCheck {

    String name();

    HealthStatus check();
}
