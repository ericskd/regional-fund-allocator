package allocator.model;

/**
 * one region's Sector Z (wholesale and retail trade) figures for one year.
 */
public record RegionData(String region, int year, int firms,
                         double turnover, double valueAdded, int employed) {
    public RegionData {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region name is required");
        }
        if (firms < 0 || employed <= 0) {
            throw new IllegalArgumentException("Invalid firm or employment count for " + region);
        }
    }

    /** Value added per person employed, in thousand euros. */
    public double productivity() {
        return valueAdded / employed;
    }
}
