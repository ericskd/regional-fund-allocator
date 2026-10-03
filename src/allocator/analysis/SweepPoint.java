package allocator.analysis;

/**
 * @param parameter            the value being swept (e.g. blend weight or floor)
 * @param hhi                  concentration of the allocation (sum of squared weights)
 * @param effectiveRegions     1 / HHI
 * @param weightedProductivity budget-weighted value added per worker (k EUR), the "return" side
 * @param largestRegion        region receiving the most money
 * @param largestShare         that region's share of the budget
 */
public record SweepPoint(double parameter, double hhi, double effectiveRegions,
                         double weightedProductivity, String largestRegion, double largestShare) {
}
