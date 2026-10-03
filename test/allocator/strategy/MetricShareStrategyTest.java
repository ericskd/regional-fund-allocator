package allocator.strategy;

import allocator.TestData;
import allocator.model.Metric;
import allocator.model.RegionData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricShareStrategyTest {

    @Test
    void sharesAreProportionalToTheMetric() {
        Map<String, Double> w = new MetricShareStrategy(Metric.VALUE_ADDED).weights(TestData.threeRegions());
        assertEquals(0.5, w.get("A"), 1e-12);
        assertEquals(0.3, w.get("B"), 1e-12);
        assertEquals(0.2, w.get("C"), 1e-12);
    }

    @Test
    void weightsSumToOne() {
        Map<String, Double> w = new MetricShareStrategy(Metric.EMPLOYED).weights(TestData.threeRegions());
        assertEquals(1.0, w.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void rejectsNegativeValues() {
        // The real 2022 ELSTAT data has negative value added for two regions
        List<RegionData> regions = List.of(TestData.region("A", 50, 10), TestData.region("B", -10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new MetricShareStrategy(Metric.VALUE_ADDED).weights(regions));
    }
}
