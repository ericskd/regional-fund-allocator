package allocator.analysis;

import allocator.model.Allocation;
import allocator.model.RegionData;
import allocator.strategy.AllocationStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Measures how sensitive an allocation is to errors in the input data.
 *
 * ELSTAT's figures are estimates (large firms are surveyed, the rest is extrapolated),
 * so we treat each region's value added, turnover and employment as
 * "true value x (1 + sigma x Z)", with Z drawn from a standard normal distribution,
 * then re-run the strategy thousands of times and look at the spread of results.
 */
public final class MonteCarloSimulator {

    private final double sigma;
    private final int simulations;
    private final long seed;

    /**
     * @param sigma        relative measurement error, e.g. 0.05 = 5% standard deviation
     * @param simulations  number of random scenarios to run
     * @param seed         fixed seed so the results are reproducible run after run
     */
    public MonteCarloSimulator(double sigma, int simulations, long seed) {
        if (sigma < 0 || sigma > 0.2) {
            throw new IllegalArgumentException("Sigma should be between 0 and 0.2");
        }
        if (simulations < 100) {
            throw new IllegalArgumentException("Use at least 100 simulations");
        }
        this.sigma = sigma;
        this.simulations = simulations;
        this.seed = seed;
    }

    public List<RegionStats> run(List<RegionData> regions, AllocationStrategy strategy, double budget) {
        Random random = new Random(seed);
        int n = regions.size();

        // Baseline: the allocation with the official data, and each region's rank in it
        Allocation baseline = Allocation.run(strategy, regions, budget);
        Map<String, Integer> baselineRanks = ranks(baseline, regions);

        // samples[i][s] = amount region i received in simulation s
        double[][] samples = new double[n][simulations];
        int[] keptRank = new int[n];

        for (int s = 0; s < simulations; s++) {
            List<RegionData> noisy = new ArrayList<>();
            for (RegionData r : regions) {
                noisy.add(perturb(r, random));
            }
            Allocation a = Allocation.run(strategy, noisy, budget);
            Map<String, Integer> simRanks = ranks(a, noisy);

            for (int i = 0; i < n; i++) {
                String name = regions.get(i).region();
                samples[i][s] = a.amount(name);
                if (simRanks.get(name).equals(baselineRanks.get(name))) {
                    keptRank[i]++;
                }
            }
        }

        List<RegionStats> stats = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = regions.get(i).region();
            double[] sorted = samples[i].clone();
            Arrays.sort(sorted);
            stats.add(new RegionStats(name,
                    baseline.amount(name),
                    mean(sorted),
                    percentile(sorted, 0.05),
                    percentile(sorted, 0.95),
                    baselineRanks.get(name),
                    (double) keptRank[i] / simulations));
        }
        stats.sort(Comparator.comparingInt(RegionStats::baselineRank));
        return stats;
    }

    /** One noisy copy of a region: each figure multiplied by (1 + sigma x Z). */
    private RegionData perturb(RegionData r, Random random) {
        return new RegionData(
                r.region(),
                r.year(),
                r.firms(),
                r.turnover() * noise(random),
                r.valueAdded() * noise(random),
                (int) Math.max(1, Math.round(r.employed() * noise(random))));
    }

    private double noise(Random random) {
        // At sigma = 5%, a negative factor needs Z below -20: effectively impossible,
        // but Math.max keeps the data valid even if someone sets sigma much higher.
        return Math.max(0.0, 1 + sigma * random.nextGaussian());
    }

    /** Rank of every region in an allocation, 1 = largest amount. */
    private static Map<String, Integer> ranks(Allocation a, List<RegionData> regions) {
        List<String> names = new ArrayList<>();
        for (RegionData r : regions) {
            names.add(r.region());
        }
        names.sort(Comparator.comparingDouble(a::weight).reversed());

        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            result.put(names.get(i), i + 1);
        }
        return result;
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** Nearest-rank percentile on an already-sorted array, e.g. p = 0.05 for the 5th percentile. */
    private static double percentile(double[] sorted, double p) {
        int index = (int) Math.round(p * (sorted.length - 1));
        return sorted[index];
    }
}
