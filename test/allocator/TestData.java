package allocator;

import allocator.io.CsvLoader;
import allocator.model.Dataset;
import allocator.model.RegionData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class TestData {

    private TestData() {
    }

    public static RegionData region(String name, double valueAdded, int employed) {
        return new RegionData(name, 2023, 10, 100.0, valueAdded, employed);
    }

    /** three regions with easy numbers: value added 50/30/20, employment 20/30/50. */
    public static List<RegionData> threeRegions() {
        return List.of(
                region("A", 50, 20),
                region("B", 30, 30),
                region("C", 20, 50));
    }

    public static Dataset realData() throws IOException {
        return CsvLoader.load(Path.of("data", "sector_z_2021_2023.csv"));
    }
}
