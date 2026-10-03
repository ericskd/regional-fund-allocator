package allocator.strategy;

import allocator.model.RegionData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * guarantees every region a minimum share, then splits the rest using another strategy:
 *   weight_i = floor + (1 - n * floor) * base_i
 */
public final class FloorStrategy implements AllocationStrategy {

    private final AllocationStrategy base;
    private final double floor;

    public FloorStrategy(AllocationStrategy base, double floor) {
        if (floor < 0) {
            throw new IllegalArgumentException("Floor cannot be negative");
        }
        this.base = base;
        this.floor = floor;
    }

    @Override
    public Map<String, Double> weights(List<RegionData> regions) {
        int n = regions.size();
        if (floor * n >= 1) {
            throw new IllegalArgumentException("A floor of " + floor + " across " + n + " regions uses up the whole budget");
        }
        double remaining = 1 - n * floor;

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : base.weights(regions).entrySet()) {
            result.put(e.getKey(), floor + remaining * e.getValue());
        }
        return result;
    }

    @Override
    public String name() {
        return base.name() + " + " + Math.round(floor * 100) + "% floor";
    }
}
