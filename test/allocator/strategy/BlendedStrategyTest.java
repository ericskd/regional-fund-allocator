package allocator.strategy;

import allocator.TestData;
import allocator.model.Metric;
import allocator.model.RegionData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlendedStrategyTest {

    @Test
    void weightOfOneEqualsTheFirstMetricAlone() {
        List<RegionData> regions = TestData.threeRegions();
        Map<String, Double> blended = new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 1.0).weights(regions);
        Map<String, Double> pure = new MetricShareStrategy(Metric.VALUE_ADDED).weights(regions);
        for (String r : pure.keySet()) {
            assertEquals(pure.get(r), blended.get(r), 1e-12);
        }
    }

    @Test
    void halfAndHalfAveragesTheTwoShares() {
        // A: (0.5 + 0.2) / 2 = 0.35
        Map<String, Double> w = new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 0.5)
                .weights(TestData.threeRegions());
        assertEquals(0.35, w.get("A"), 1e-12);
    }

    @Test
    void reproducesTheOlympiadResultOnRealData() throws IOException {
        // The team's submitted answer: Attica 57.55%, Central Macedonia 15.75%, North Aegean 0.87%
        List<RegionData> y2023 = TestData.realData().forYear(2023);
        Map<String, Double> w = new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 0.5).weights(y2023);
        assertEquals(0.5755, w.get("Attica"), 0.00005);
        assertEquals(0.1575, w.get("Central Macedonia"), 0.00005);
        assertEquals(0.0087, w.get("North Aegean"), 0.00005);
    }

    @Test
    void rejectsWeightOutsideZeroToOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 1.2));
    }
}
