package allocator.model;

import allocator.strategy.AllocationStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * The result of running one strategy: each region's weight (share of the budget)
 * plus portfolio-style measures of how concentrated the allocation is.
 */
public final class Allocation {

    private static final double TOLERANCE = 1e-9;

    private final String strategyName;
    private final double budget;
    private final Map<String, Double> weights;

    private Allocation(String strategyName, double budget, Map<String, Double> weights) {
        double sum = 0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            if (e.getValue() < 0) {
                throw new IllegalStateException("Negative weight for " + e.getKey());
            }
            sum += e.getValue();
        }
        if (Math.abs(sum - 1.0) > TOLERANCE) {
            throw new IllegalStateException("Weights must sum to 1 but sum to " + sum);
        }
        this.strategyName = strategyName;
        this.budget = budget;
        this.weights = Collections.unmodifiableMap(weights);
    }

    /** Runs a strategy on a set of regions and checks the output is a valid allocation. */
    public static Allocation run(AllocationStrategy strategy, List<RegionData> regions, double budget) {
        if (budget <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        return new Allocation(strategy.name(), budget, strategy.weights(regions));
    }

    public double weight(String region) {
        Double w = weights.get(region);
        if (w == null) {
            throw new IllegalArgumentException("Unknown region: " + region);
        }
        return w;
    }

    public double amount(String region) {
        return weight(region) * budget;
    }

    /**
     * Herfindahl-Hirschman Index: the sum of squared weights.
     * 1/n means perfectly equal, 1.0 means everything in one region.
     */
    public double hhi() {
        double h = 0;
        for (double w : weights.values()) {
            h += w * w;
        }
        return h;
    }

    /** 1 / HHI: how many equally funded regions this allocation "behaves like". */
    public double effectiveRegions() {
        return 1.0 / hhi();
    }

    /**
     * Budget-weighted average of any regional figure: sum of weight_i * value_i.
     * Example: weightedAverage(regions, RegionData::productivity) tells you how
     * productive the regions receiving the money are, on average.
     */
    public double weightedAverage(List<RegionData> regions, ToDoubleFunction<RegionData> value) {
        double total = 0;
        for (RegionData r : regions) {
            total += weight(r.region()) * value.applyAsDouble(r);
        }
        return total;
    }

    /** The region receiving the largest share. */
    public String largestRegion() {
        String best = null;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            if (best == null || e.getValue() > weights.get(best)) {
                best = e.getKey();
            }
        }
        return best;
    }

    public String strategyName() {
        return strategyName;
    }

    public double budget() {
        return budget;
    }

    public Map<String, Double> weights() {
        return weights;
    }
}
