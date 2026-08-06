package studio.sniffa.common.health;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a fixed set of {@link HealthCheck}s and combines their results - shared by every service
 * that needs to answer "am I healthy" (sniffa-backend's HTTP /health, sniffa-node-agent's
 * self-check before reporting a server as RUNNING) instead of each hand-rolling its own
 * "loop over checks, AND the results together" logic.
 */
public final class HealthAggregator {

    private final List<HealthCheck> checks;

    public HealthAggregator(List<HealthCheck> checks) {
        this.checks = List.copyOf(checks);
    }

    public AggregateHealth check() {
        Map<String, HealthStatus> results = new LinkedHashMap<>();
        boolean allHealthy = true;
        for (HealthCheck check : checks) {
            HealthStatus status = check.check();
            results.put(check.name(), status);
            allHealthy &= status.healthy();
        }
        return new AggregateHealth(allHealthy, results);
    }
}
