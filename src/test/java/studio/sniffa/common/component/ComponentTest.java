package studio.sniffa.common.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentTest {

    @Test
    void defaultStartAndStopAreNoOps() {
        Component component = () -> "test-component";

        assertEquals("test-component", component.name());
        assertDoesNotThrow(component::start);
        assertDoesNotThrow(component::stop);
    }
}
