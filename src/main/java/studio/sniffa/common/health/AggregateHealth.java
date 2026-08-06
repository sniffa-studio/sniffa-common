package studio.sniffa.common.health;

import java.util.Map;

/** The combined result of running every registered {@link HealthCheck}. */
public record AggregateHealth(boolean healthy, Map<String, HealthStatus> results) {
}
