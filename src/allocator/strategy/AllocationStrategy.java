package allocator.strategy;

import allocator.model.RegionData;

import java.util.List;
import java.util.Map;

/**
 * A rule for splitting a budget across regions.
 * Every strategy returns weights (shares of the budget) that are
 * non-negative and sum to 1. Allocation.run() enforces this.
 */
public interface AllocationStrategy {

    Map<String, Double> weights(List<RegionData> regions);

    String name();
}
