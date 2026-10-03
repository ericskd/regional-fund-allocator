package allocator.analysis;

import allocator.model.Allocation;
import allocator.model.RegionData;
import allocator.strategy.AllocationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

/**
 * runs the same type of strategy many times while changing one parameter,
 * and records how concentration (risk side) and productivity (return side) respond.
 */
public final class SensitivitySweep {

    private SensitivitySweep() {
    }

    /**
     * @param regions  the regions to allocate across
     * @param factory  builds a strategy from a parameter value, e.g. w -> new BlendedStrategy(..., w)
     * @param from     first parameter value
     * @param to       last parameter value
     * @param steps    number of intervals between from and to (steps + 1 points in total)
     */
    public static List<SweepPoint> run(List<RegionData> regions,
                                       DoubleFunction<AllocationStrategy> factory,
                                       double from, double to, int steps) {
        if (steps < 1) {
            throw new IllegalArgumentException("Need at least one step");
        }
        List<SweepPoint> points = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            // Computed from i each time instead of adding 0.1 repeatedly:
            // 0.1 has no exact binary representation, so repeated addition drifts.
            double p = from + (to - from) * i / steps;

            Allocation a = Allocation.run(factory.apply(p), regions, 1.0);
            String top = a.largestRegion();
            points.add(new SweepPoint(p, a.hhi(), a.effectiveRegions(),
                    a.weightedAverage(regions, RegionData::productivity),
                    top, a.weight(top)));
        }
        return points;
    }
}
