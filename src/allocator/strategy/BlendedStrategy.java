package allocator.strategy;

import allocator.model.Metric;
import allocator.model.RegionData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Weighted mix of two metric shares:
 *   weight_i = w * share_i(first) + (1 - w) * share_i(second)
 * With VALUE_ADDED, EMPLOYED and w = 0.5 this is the Olympiad team's model.
 */
public final class BlendedStrategy implements AllocationStrategy {

    private final Metric first;
    private final Metric second;
    private final double weightOnFirst;

    public BlendedStrategy(Metric first, Metric second, double weightOnFirst) {
        if (weightOnFirst < 0 || weightOnFirst > 1) {
            throw new IllegalArgumentException("Blend weight must be between 0 and 1");
        }
        this.first = first;
        this.second = second;
        this.weightOnFirst = weightOnFirst;
    }

    @Override
    public Map<String, Double> weights(List<RegionData> regions) {
        Map<String, Double> a = new MetricShareStrategy(first).weights(regions);
        Map<String, Double> b = new MetricShareStrategy(second).weights(regions);

        Map<String, Double> blended = new LinkedHashMap<>();
        for (String region : a.keySet()) {
            blended.put(region, weightOnFirst * a.get(region) + (1 - weightOnFirst) * b.get(region));
        }
        return blended;
    }

    @Override
    public String name() {
        long pctFirst = Math.round(weightOnFirst * 100);
        return pctFirst + "/" + (100 - pctFirst) + " " + first.label() + "/" + second.label();
    }
}
