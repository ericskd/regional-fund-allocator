package allocator;

import allocator.analysis.BacktestResult;
import allocator.analysis.Backtester;
import allocator.analysis.MonteCarloSimulator;
import allocator.analysis.RegionStats;
import allocator.analysis.SensitivitySweep;
import allocator.analysis.SweepPoint;
import allocator.io.CsvLoader;
import allocator.io.CsvWriter;
import allocator.model.Allocation;
import allocator.model.Dataset;
import allocator.model.Metric;
import allocator.model.RegionData;
import allocator.strategy.AllocationStrategy;
import allocator.strategy.BlendedStrategy;
import allocator.strategy.EqualStrategy;
import allocator.strategy.FloorStrategy;
import allocator.strategy.MetricShareStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    private static final Path DATA_FILE = Path.of("data", "sector_z_2021_2023.csv");
    private static final Path RESULTS_DIR = Path.of("results");
    private static final double BUDGET_EUR_M = 100.0;   // the Ministry's budget, in EUR millions
    private static final int YEAR = 2023;

    public static void main(String[] args) throws IOException {
        Dataset data = CsvLoader.load(DATA_FILE);
        List<RegionData> regions = data.forYear(YEAR);

        runComparison(regions);
        runSensitivity(regions);
        runMonteCarlo(regions);
        runBacktest(data);
    }

    private static void runComparison(List<RegionData> regions) {
        AllocationStrategy olympiad = olympiadModel();
        List<AllocationStrategy> strategies = List.of(
                new MetricShareStrategy(Metric.VALUE_ADDED),
                new MetricShareStrategy(Metric.EMPLOYED),
                olympiad,
                new FloorStrategy(olympiad, 0.02),
                new EqualStrategy());

        List<Allocation> results = new ArrayList<>();
        for (AllocationStrategy s : strategies) {
            results.add(Allocation.run(s, regions, BUDGET_EUR_M));
        }

        Allocation reference = results.get(2);
        List<RegionData> sorted = new ArrayList<>(regions);
        sorted.sort(Comparator.comparingDouble((RegionData r) -> reference.weight(r.region())).reversed());

        System.out.println("=== STAGE 1: Sector Z funding allocation, " + YEAR + ", budget EUR " + BUDGET_EUR_M + "m ===");
        System.out.println();

        StringBuilder header = new StringBuilder(String.format("%-30s", "Region (EUR m)"));
        for (int i = 0; i < results.size(); i++) {
            header.append(String.format("%10s", "[" + (char) ('A' + i) + "]"));
        }
        System.out.println(header);

        for (RegionData r : sorted) {
            StringBuilder line = new StringBuilder(String.format("%-30s", r.region()));
            for (Allocation a : results) {
                line.append(String.format("%10.2f", a.amount(r.region())));
            }
            System.out.println(line);
        }

        System.out.println();
        for (int i = 0; i < results.size(); i++) {
            Allocation a = results.get(i);
            System.out.println(String.format("[%c] %-45s HHI %.3f   effective regions %.1f   productivity %.2fk",
                    (char) ('A' + i), a.strategyName(), a.hhi(), a.effectiveRegions(),
                    a.weightedAverage(regions, RegionData::productivity)));
        }
    }



    private static void runSensitivity(List<RegionData> regions) throws IOException {

        List<SweepPoint> weightSweep = SensitivitySweep.run(regions,
                w -> new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, w),
                0.0, 1.0, 10);


        AllocationStrategy olympiad = olympiadModel();
        List<SweepPoint> floorSweep = SensitivitySweep.run(regions,
                f -> new FloorStrategy(olympiad, f),
                0.0, 0.07, 14);

        System.out.println();
        System.out.println("=== STAGE 2a: blend weight on value added (0 = all employment, 1 = all value added) ===");
        printSweep("weight", weightSweep);

        System.out.println();
        System.out.println("=== STAGE 2b: minimum floor per region, on top of the 50/50 Olympiad blend ===");
        printSweep("floor", floorSweep);

        CsvWriter.writeSweep(RESULTS_DIR.resolve("sweep_blend_weight.csv"), "weight_on_value_added", weightSweep);
        CsvWriter.writeSweep(RESULTS_DIR.resolve("sweep_floor.csv"), "floor", floorSweep);
        System.out.println();
        System.out.println("Sweep results saved to " + RESULTS_DIR.toAbsolutePath());
    }

    private static void printSweep(String parameterName, List<SweepPoint> points) {
        System.out.println(String.format("%8s %8s %10s %14s %14s",
                parameterName, "HHI", "eff. reg.", "productivity", "Attica share"));
        for (SweepPoint p : points) {
            System.out.println(String.format("%8.3f %8.3f %10.2f %13.2fk %13.1f%%",
                    p.parameter(), p.hhi(), p.effectiveRegions(),
                    p.weightedProductivity(), p.largestShare() * 100));
        }
    }



    private static final double SIGMA = 0.05;        
    private static final int SIMULATIONS = 10_000;
    private static final long SEED = 42;             

    private static void runMonteCarlo(List<RegionData> regions) throws IOException {
        MonteCarloSimulator simulator = new MonteCarloSimulator(SIGMA, SIMULATIONS, SEED);
        List<RegionStats> stats = simulator.run(regions, olympiadModel(), BUDGET_EUR_M);

        System.out.println();
        System.out.println("=== STAGE 3: Monte Carlo, Olympiad model, " + SIMULATIONS
                + " runs, " + Math.round(SIGMA * 100) + "% data error ===");
        System.out.println(String.format("%-30s %5s %9s %9s %21s %8s %10s",
                "Region (EUR m)", "rank", "baseline", "mean", "90% interval", "width", "same rank"));
        for (RegionStats r : stats) {
            System.out.println(String.format("%-30s %5d %9.2f %9.2f %10.2f - %8.2f %7.1f%% %9.1f%%",
                    r.region(), r.baselineRank(), r.baseline(), r.mean(), r.p05(), r.p95(),
                    r.relativeWidth() * 100, r.rankStability() * 100));
        }

        CsvWriter.writeMonteCarlo(RESULTS_DIR.resolve("monte_carlo_olympiad.csv"), stats);
    }


    private static final int PERMUTATIONS = 10_000;

    private static void runBacktest(Dataset data) throws IOException {
        AllocationStrategy olympiad = olympiadModel();
        List<AllocationStrategy> strategies = List.of(
                new MetricShareStrategy(Metric.VALUE_ADDED),
                new MetricShareStrategy(Metric.EMPLOYED),
                olympiad,
                new FloorStrategy(olympiad, 0.02),
                new EqualStrategy());

        for (Metric outcome : List.of(Metric.VALUE_ADDED, Metric.EMPLOYED)) {
            Backtester backtester = new Backtester(data, 2021, 2023, outcome);
            List<BacktestResult> results = new ArrayList<>();
            for (AllocationStrategy s : strategies) {
                results.add(backtester.evaluate(s, PERMUTATIONS, SEED));
            }

            System.out.println();
            System.out.println("=== STAGE 4: backtest, rules built on 2021 data, scored on "
                    + backtester.description() + " ===");
            System.out.println(String.format("%-45s %10s %10s %8s %8s",
                    "Strategy", "growth", "vs equal", "IC", "p"));
            for (BacktestResult r : results) {
                System.out.println(String.format("%-45s %9.1f%% %+9.1f%% %8.2f %8.3f",
                        r.strategyName(), r.weightedGrowth() * 100, r.excessGrowth() * 100,
                        r.ic(), r.pValue()));
            }
            System.out.println(String.format("%-45s %9.1f%%", "Equal split (benchmark)",
                    backtester.benchmarkGrowth() * 100));

            String file = "backtest_" + outcome.name().toLowerCase() + ".csv";
            CsvWriter.writeBacktest(RESULTS_DIR.resolve(file), backtester.description(), results);
        }

    
        try {
            new Backtester(data, 2022, 2023, Metric.VALUE_ADDED);
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("Data check, 2022 formation year rejected: " + e.getMessage());
        }
    }

 
    private static AllocationStrategy olympiadModel() {
        return new BlendedStrategy(Metric.VALUE_ADDED, Metric.EMPLOYED, 0.5);
    }
}
