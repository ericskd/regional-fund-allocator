package allocator.analysis;

import allocator.TestData;
import allocator.model.Dataset;
import allocator.model.Metric;
import allocator.model.RegionData;
import allocator.strategy.BlendedStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BacktesterAndMonteCarloTest {

    private final BlendedStrategy olympiad = new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 0.5);

    @Test
    void backtestRefusesTheNegative2022ValueAdded() throws IOException {
        Dataset data = TestData.realData();
        assertThrows(IllegalArgumentException.class,
                () -> new Backtester(data, 2022, 2023, Metric.VALUE_ADDED));
    }

    @Test
    void sameSeedGivesIdenticalMonteCarloResults() throws IOException {
        List<RegionData> y2023 = TestData.realData().forYear(2023);
        List<RegionStats> first = new MonteCarloSimulator(0.05, 500, 7).run(y2023, olympiad, 100);
        List<RegionStats> second = new MonteCarloSimulator(0.05, 500, 7).run(y2023, olympiad, 100);
        assertEquals(first, second);   // records compare field by field
    }

    @Test
    void zeroNoiseCollapsesTheIntervalOntoTheBaseline() throws IOException {
        List<RegionData> y2023 = TestData.realData().forYear(2023);
        for (RegionStats r : new MonteCarloSimulator(0.0, 200, 1).run(y2023, olympiad, 100)) {
            assertEquals(r.baseline(), r.p05(), 1e-9);
            assertEquals(r.baseline(), r.p95(), 1e-9);
            assertEquals(1.0, r.rankStability(), 1e-12);
        }
    }
}
