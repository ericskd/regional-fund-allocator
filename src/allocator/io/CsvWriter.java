package allocator.io;

import allocator.analysis.BacktestResult;
import allocator.analysis.RegionStats;
import allocator.analysis.SweepPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Saves results so they can be charted in Excel or shown in the README. */
public final class CsvWriter {

    private CsvWriter() {
    }

    public static void writeSweep(Path path, String parameterName, List<SweepPoint> points) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(parameterName + ",hhi,effective_regions,weighted_productivity_k_eur,largest_region,largest_share");
        for (SweepPoint p : points) {
            // Locale.ROOT forces "0.5" rather than "0,5", which would break the CSV on a Greek-locale PC
            lines.add(String.format(Locale.ROOT, "%.4f,%.6f,%.4f,%.4f,%s,%.6f",
                    p.parameter(), p.hhi(), p.effectiveRegions(),
                    p.weightedProductivity(), p.largestRegion(), p.largestShare()));
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static void writeMonteCarlo(Path path, List<RegionStats> stats) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("region,baseline,mean,p05,p95,baseline_rank,rank_stability");
        for (RegionStats r : stats) {
            lines.add(String.format(Locale.ROOT, "%s,%.4f,%.4f,%.4f,%.4f,%d,%.4f",
                    r.region(), r.baseline(), r.mean(), r.p05(), r.p95(),
                    r.baselineRank(), r.rankStability()));
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static void writeBacktest(Path path, String outcome, List<BacktestResult> results) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("outcome,strategy,weighted_growth,excess_growth,ic,p_value");
        for (BacktestResult r : results) {
            lines.add(String.format(Locale.ROOT, "%s,%s,%.6f,%.6f,%.4f,%.4f",
                    outcome, r.strategyName(), r.weightedGrowth(), r.excessGrowth(), r.ic(), r.pValue()));
        }
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
