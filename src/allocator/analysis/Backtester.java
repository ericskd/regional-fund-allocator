package allocator.analysis;

import allocator.model.Allocation;
import allocator.model.Dataset;
import allocator.model.Metric;
import allocator.model.RegionData;
import allocator.strategy.AllocationStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Backtester {

    private final List<RegionData> formation;
    private final Map<String, Double> growth = new LinkedHashMap<>();
    private final String description;

    public Backtester(Dataset data, int formationYear, int outcomeYear, Metric outcome) {
        if (outcomeYear <= formationYear) {
            throw new IllegalArgumentException("Outcome year must come after formation year");
        }
        this.formation = data.forYear(formationYear);
        this.description = outcome.label() + " growth " + formationYear + " to " + outcomeYear;

        for (RegionData start : formation) {
            RegionData end = data.find(start.region(), outcomeYear)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No " + outcomeYear + " data for " + start.region()));
            double base = outcome.of(start);
            if (base <= 0) {
     
                throw new IllegalArgumentException("Growth undefined: " + outcome.label()
                        + " is " + base + " for " + start.region() + " in " + formationYear);
            }
            growth.put(start.region(), outcome.of(end) / base - 1);
        }
    }


    public double benchmarkGrowth() {
        double sum = 0;
        for (double g : growth.values()) {
            sum += g;
        }
        return sum / growth.size();
    }

    public BacktestResult evaluate(AllocationStrategy strategy, int permutations, long seed) {
        Allocation a = Allocation.run(strategy, formation, 1.0);

        int n = formation.size();
        double[] weights = new double[n];
        double[] outcomes = new double[n];
        double weightedGrowth = 0;
        for (int i = 0; i < n; i++) {
            String region = formation.get(i).region();
            weights[i] = a.weight(region);
            outcomes[i] = growth.get(region);
            weightedGrowth += weights[i] * outcomes[i];
        }

        double ic = RankCorrelation.spearman(weights, outcomes);
        double p = RankCorrelation.permutationPValue(weights, outcomes, permutations, seed);
        return new BacktestResult(strategy.name(), weightedGrowth,
                weightedGrowth - benchmarkGrowth(), ic, p);
    }

    public String description() {
        return description;
    }
}
