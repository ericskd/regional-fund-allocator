package allocator.model;

import allocator.TestData;
import allocator.strategy.AllocationStrategy;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllocationTest {

    private static AllocationStrategy fixed(Map<String, Double> weights) {
        return new AllocationStrategy() {
            @Override
            public Map<String, Double> weights(List<RegionData> regions) {
                return new LinkedHashMap<>(weights);
            }

            @Override
            public String name() {
                return "fixed";
            }
        };
    }

    @Test
    void equalSplitHasHhiOfOneOverN() {
        Allocation a = Allocation.run(fixed(Map.of("A", 0.25, "B", 0.25, "C", 0.25, "D", 0.25)),
                TestData.threeRegions(), 100);
        assertEquals(0.25, a.hhi(), 1e-12);
        assertEquals(4.0, a.effectiveRegions(), 1e-12);
    }

    @Test
    void everythingInOneRegionHasHhiOfOne() {
        Allocation a = Allocation.run(fixed(Map.of("A", 1.0, "B", 0.0)), TestData.threeRegions(), 100);
        assertEquals(1.0, a.hhi(), 1e-12);
    }

    @Test
    void amountIsWeightTimesBudget() {
        Allocation a = Allocation.run(fixed(Map.of("A", 0.6, "B", 0.4)), TestData.threeRegions(), 100);
        assertEquals(60.0, a.amount("A"), 1e-12);
    }

    @Test
    void rejectsWeightsThatDoNotSumToOne() {
        assertThrows(IllegalStateException.class,
                () -> Allocation.run(fixed(Map.of("A", 0.6, "B", 0.6)), TestData.threeRegions(), 100));
    }
}
