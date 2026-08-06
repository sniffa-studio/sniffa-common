package studio.sniffa.common.health;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthAggregatorTest {

    @Test
    void allHealthyMeansAggregateHealthy() {
        HealthAggregator aggregator = new HealthAggregator(List.of(
                check("a", HealthStatus.ok()),
                check("b", HealthStatus.ok())
        ));

        AggregateHealth result = aggregator.check();

        assertTrue(result.healthy());
        assertTrue(result.results().get("a").healthy());
        assertTrue(result.results().get("b").healthy());
    }

    @Test
    void oneUnhealthyMeansAggregateUnhealthy() {
        HealthAggregator aggregator = new HealthAggregator(List.of(
                check("a", HealthStatus.ok()),
                check("b", HealthStatus.unhealthy("db down"))
        ));

        AggregateHealth result = aggregator.check();

        assertFalse(result.healthy());
        assertFalse(result.results().get("b").healthy());
        assertTrue(result.results().containsKey("a"));
    }

    private HealthCheck check(String name, HealthStatus status) {
        return new HealthCheck() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public HealthStatus check() {
                return status;
            }
        };
    }
}
