package studio.sniffa.common.health;

/** The result of a single {@link HealthCheck}. */
public record HealthStatus(boolean healthy, String detail) {

    public static HealthStatus ok() {
        return new HealthStatus(true, null);
    }

    public static HealthStatus unhealthy(String detail) {
        return new HealthStatus(false, detail);
    }
}
