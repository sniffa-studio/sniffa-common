package studio.sniffa.common.id;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeOrderedIdTest {

    @Test
    void hasFixedLength() {
        assertEquals(26, TimeOrderedId.generate().length());
    }

    @Test
    void usesOnlyCrockfordAlphabet() {
        assertTrue(TimeOrderedId.generate().chars().allMatch(c -> "0123456789ABCDEFGHJKMNPQRSTVWXYZ".indexOf(c) >= 0));
    }

    @Test
    void laterInstantSortsAfterEarlierInstant() {
        String earlier = TimeOrderedId.generate(Instant.parse("2026-01-01T00:00:00Z"));
        String later = TimeOrderedId.generate(Instant.parse("2026-01-01T00:00:01Z"));
        assertTrue(earlier.compareTo(later) < 0);
    }

    @Test
    void sameMillisecondStillProducesDistinctIds() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            ids.add(TimeOrderedId.generate(instant));
        }
        assertEquals(100, ids.size());
    }
}
