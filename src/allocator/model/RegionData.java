package allocator.model;

/**
 * One region's Sector Z (wholesale and retail trade) figures for one year.
 * Source: ELSTAT Structural Business Statistics. Money values are in thousand euros.
 *
 * A record is an immutable data class: Java generates the constructor,
 * the getters (region(), year(), ...), equals, hashCode and toString for us.
 */
public record RegionData(String region, int year, int firms,
                         double turnover, double valueAdded, int employed) {

    /** Compact constructor: runs before the fields are set, so bad data never gets in. */
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
