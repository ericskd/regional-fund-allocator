package allocator.model;

import java.util.function.ToDoubleFunction;

/**
 * The variables a strategy can allocate on. Each constant knows how to read
 * its own value from a RegionData, so strategies never hard-code a column.
 */
public enum Metric {
    FIRMS("Firms", r -> r.firms()),
    TURNOVER("Turnover", RegionData::turnover),
    VALUE_ADDED("Value added", RegionData::valueAdded),
    EMPLOYED("Employment", r -> r.employed());

    private final String label;
    private final ToDoubleFunction<RegionData> extractor;

    Metric(String label, ToDoubleFunction<RegionData> extractor) {
        this.label = label;
        this.extractor = extractor;
    }

    /** Reads this metric's value from one region, e.g. Metric.VALUE_ADDED.of(attica). */
    public double of(RegionData region) {
        return extractor.applyAsDouble(region);
    }

    public String label() {
        return label;
    }
}
