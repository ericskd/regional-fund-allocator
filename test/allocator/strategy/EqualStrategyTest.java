package allocator.strategy;

import allocator.TestData;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EqualStrategyTest {

    @Test
    void everyRegionGetsOneOverN() {
        Map<String, Double> w = new EqualStrategy().weights(TestData.threeRegions());
        for (double weight : w.values()) {
            assertEquals(1.0 / 3, weight, 1e-12);
        }
    }
}
