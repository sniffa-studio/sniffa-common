package studio.sniffa.common.shutdown;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShutdownHooksTest {

    @Test
    void runsHooksInRegistrationOrder() {
        List<String> ran = new ArrayList<>();
        ShutdownHooks hooks = new ShutdownHooks()
                .register("first", () -> ran.add("first"))
                .register("second", () -> ran.add("second"));

        hooks.runAll();

        assertEquals(List.of("first", "second"), ran);
    }

    @Test
    void aFailingHookDoesNotStopLaterHooks() {
        List<String> ran = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        ShutdownHooks hooks = new ShutdownHooks((name, e) -> failed.add(name))
                .register("broken", () -> {
                    throw new RuntimeException("boom");
                })
                .register("fine", () -> ran.add("fine"));

        hooks.runAll();

        assertEquals(List.of("fine"), ran);
        assertEquals(List.of("broken"), failed);
    }
}
