package allocator.analysis;

/**
 * Monte Carlo summary for one region, all amounts in the same unit as the budget.
 *
 * @param region         region name
 * @param baseline       allocation using the official (un-noised) data
 * @param mean           average allocation across all simulations
 * @param p05            5th percentile: only 5% of simulations gave less
 * @param p95            95th percentile: only 5% of simulations gave more
 * @param baselineRank   rank with the official data (1 = largest)
 * @param rankStability  share of simulations in which the region kept that rank
 */
public record RegionStats(String region, double baseline, double mean, double p05, double p95,
                          int baselineRank, double rankStability) {

    /** width of the 90% interval relative to the baseline, e.g. 0.12 = plus or minus about 6%. */
    public double relativeWidth() {
        return (p95 - p05) / baseline;
    }
}
