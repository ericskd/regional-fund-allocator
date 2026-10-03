package allocator.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankCorrelationTest {

    @Test
    void tiedValuesShareTheAverageRank() {
        assertArrayEquals(new double[]{1, 2.5, 2.5, 4},
                RankCorrelation.ranks(new double[]{10, 20, 20, 30}), 1e-12);
    }

    @Test
    void sameOrderGivesPlusOne() {
        assertEquals(1.0, RankCorrelation.spearman(new double[]{1, 2, 3, 4}, new double[]{10, 20, 30, 40}), 1e-12);
    }

    @Test
    void reversedOrderGivesMinusOne() {
        assertEquals(-1.0, RankCorrelation.spearman(new double[]{1, 2, 3, 4}, new double[]{40, 30, 20, 10}), 1e-12);
    }

    @Test
    void spearmanOnlyCaresAboutOrderNotSize() {
        // y = x cubed is very non-linear, but the order is identical, so Spearman = 1 (Pearson would not be)
        double[] x = {1, 2, 3, 4, 5};
        double[] y = {1, 8, 27, 64, 125};
        assertEquals(1.0, RankCorrelation.spearman(x, y), 1e-12);
        assertTrue(RankCorrelation.pearson(x, y) < 1.0);
    }

    @Test
    void constantInputHasNoCorrelation() {
        assertTrue(Double.isNaN(RankCorrelation.spearman(new double[]{1, 1, 1}, new double[]{1, 2, 3})));
    }

    @Test
    void perfectCorrelationIsRarelyMatchedByChance() {
        double[] x = {1, 2, 3, 4, 5, 6, 7, 8};
        double p = RankCorrelation.permutationPValue(x, x.clone(), 5_000, 1);
        assertTrue(p < 0.01, "p was " + p);
    }
}
