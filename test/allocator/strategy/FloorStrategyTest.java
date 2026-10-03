package allocator.strategy;

import allocator.TestData;
import allocator.model.Metric;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloorStrategyTest {

    private final AllocationStrategy base = new MetricShareStrategy(Metric.VALUE_ADDED);

    @Test
    void everyRegionGetsAtLeastTheFloor() {
        Map<String, Double> w = new FloorStrategy(base, 0.1).weights(TestData.threeRegions());
        for (double weight : w.values()) {
            assertTrue(weight >= 0.1 - 1e-12);
        }
    }

    @Test
    void formulaMatchesByHand() {
        // A: 0.1 + (1 - 3 x 0.1) x 0.5 = 0.45
        Map<String, Double> w = new FloorStrategy(base, 0.1).weights(TestData.threeRegions());
        assertEquals(0.45, w.get("A"), 1e-12);
    }

    @Test
    void stillSumsToOne() {
        Map<String, Double> w = new FloorStrategy(base, 0.2).weights(TestData.threeRegions());
        assertEquals(1.0, w.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void rejectsAFloorThatUsesUpTheWholeBudget() {
        assertThrows(IllegalArgumentException.class,
                () -> new FloorStrategy(base, 0.34).weights(TestData.threeRegions()));
    }
}
