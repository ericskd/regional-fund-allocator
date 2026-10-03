package allocator.model;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/** All regions across all years, with simple ways to slice them. */
public final class Dataset {

    private final List<RegionData> rows;

    public Dataset(List<RegionData> rows) {
        this.rows = List.copyOf(rows);   // defensive, unmodifiable copy
    }

    /** Every region's data for one year. */
    public List<RegionData> forYear(int year) {
        List<RegionData> result = rows.stream()
                .filter(r -> r.year() == year)
                .toList();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("No data for year " + year);
        }
        return result;
    }

    /** One region in one year, if it exists. */
    public Optional<RegionData> find(String region, int year) {
        return rows.stream()
                .filter(r -> r.year() == year && r.region().equals(region))
                .findFirst();
    }

    /** National total of a metric for one year. */
    public double total(int year, Metric metric) {
        return forYear(year).stream().mapToDouble(metric::of).sum();
    }

    public SortedSet<Integer> years() {
        SortedSet<Integer> years = new TreeSet<>();
        for (RegionData r : rows) {
            years.add(r.year());
        }
        return years;
    }

    public int size() {
        return rows.size();
    }
}
