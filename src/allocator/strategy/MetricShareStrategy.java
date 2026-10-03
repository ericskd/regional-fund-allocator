package allocator.strategy;

import allocator.model.Metric;
import allocator.model.RegionData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Each region gets a share equal to its share of one metric, e.g. its share of national value added. */
public final class MetricShareStrategy implements AllocationStrategy {

    private final Metric metric;

    public MetricShareStrategy(Metric metric) {
        this.metric = Objects.requireNonNull(metric);
    }

    @Override
    public Map<String, Double> weights(List<RegionData> regions) {
        double total = 0;
        for (RegionData r : regions) {
            double value = metric.of(r);
            if (value < 0) {
                throw new IllegalArgumentException(metric.label() + " is negative for " + r.region());
            }
            total += value;
        }
        if (total == 0) {
            throw new IllegalArgumentException("Total " + metric.label() + " is zero");
        }

        Map<String, Double> weights = new LinkedHashMap<>();   // keeps the regions in input order
        for (RegionData r : regions) {
            weights.put(r.region(), metric.of(r) / total);
        }
        return weights;
    }

    @Override
    public String name() {
        return metric.label() + " share";
    }
}
