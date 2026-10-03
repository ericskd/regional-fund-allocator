package allocator.analysis;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Spearman rank correlation and a permutation test for its significance.
 * Spearman = Pearson correlation applied to ranks instead of raw values,
 * so it only cares about ORDER: "did the regions we ranked higher grow faster?"
 */
public final class RankCorrelation {

    private RankCorrelation() {
    }

    /** Ranks from 1 (smallest) to n (largest); tied values share the average of their ranks. */
    public static double[] ranks(double[] values) {
        int n = values.length;
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingDouble(i -> values[i]));

        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            int j = i;
            while (j + 1 < n && values[order[j + 1]] == values[order[i]]) {
                j++;
            }
            double averageRank = (i + j) / 2.0 + 1;   // +1 because ranks start at 1
            for (int k = i; k <= j; k++) {
                ranks[order[k]] = averageRank;
            }
            i = j + 1;
        }
        return ranks;
    }

    /** Pearson correlation. Returns NaN if either input has no variation. */
    public static double pearson(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) {
            throw new IllegalArgumentException("Need two arrays of equal length (at least 2)");
        }
        double meanX = Arrays.stream(x).average().orElseThrow();
        double meanY = Arrays.stream(y).average().orElseThrow();
        double sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sxy += dx * dy;
            sxx += dx * dx;
            syy += dy * dy;
        }
        if (sxx == 0 || syy == 0) {
            return Double.NaN;   // e.g. an equal split: every weight identical, so no ranking exists
        }
        return sxy / Math.sqrt(sxx * syy);
    }

    public static double spearman(double[] x, double[] y) {
        return pearson(ranks(x), ranks(y));
    }

    /**
     * How often would pure chance produce a correlation at least this strong?
     * Shuffle y randomly many times, recompute Spearman each time, and count
     * how often |shuffled| >= |observed|. A small answer means the result is unlikely to be luck.
     */
    public static double permutationPValue(double[] x, double[] y, int trials, long seed) {
        double observed = Math.abs(spearman(x, y));
        if (Double.isNaN(observed)) {
            return Double.NaN;
        }
        Random random = new Random(seed);
        double[] shuffled = y.clone();
        int atLeastAsExtreme = 0;

        for (int t = 0; t < trials; t++) {
            shuffle(shuffled, random);
            if (Math.abs(spearman(x, shuffled)) >= observed - 1e-12) {
                atLeastAsExtreme++;
            }
        }
        // +1 on top and bottom counts the real data as one of the arrangements, so p is never exactly 0
        return (atLeastAsExtreme + 1.0) / (trials + 1.0);
    }

    /** Fisher-Yates shuffle: every ordering is equally likely. */
    private static void shuffle(double[] a, Random random) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }
}
