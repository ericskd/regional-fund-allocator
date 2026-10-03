package allocator.analysis;

/**
 * @param strategyName   the rule being tested
 * @param weightedGrowth budget-weighted growth of the regions it funded (sum of weight_i x growth_i)
 * @param excessGrowth   weightedGrowth minus an equal split's growth (the benchmark)
 * @param ic             information coefficient: Spearman correlation between weights and later growth
 * @param pValue         permutation-test p-value for the IC
 */
public record BacktestResult(String strategyName, double weightedGrowth, double excessGrowth,
                             double ic, double pValue) {
}
