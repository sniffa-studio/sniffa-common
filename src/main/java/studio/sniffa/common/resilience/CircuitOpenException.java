package studio.sniffa.common.resilience;

/** Thrown by {@link CircuitBreaker#call} while the breaker is open. */
public final class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String message) {
        super(message);
    }
}
