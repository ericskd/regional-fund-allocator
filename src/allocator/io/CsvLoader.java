package allocator.io;

import allocator.model.Dataset;
import allocator.model.RegionData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the regional data file. Expected columns:
 * year,region,firms,turnover_k_eur,value_added_k_eur,employed
 */
public final class CsvLoader {

    private static final int EXPECTED_FIELDS = 6;

    private CsvLoader() {
        // utility class: only static methods, never instantiated
    }

    public static Dataset load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<RegionData> rows = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {   // line 0 is the header
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] f = line.split(",");
            if (f.length != EXPECTED_FIELDS) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": expected "
                        + EXPECTED_FIELDS + " fields but found " + f.length);
            }
            rows.add(new RegionData(
                    f[1].trim(),
                    Integer.parseInt(f[0].trim()),
                    Integer.parseInt(f[2].trim()),
                    Double.parseDouble(f[3].trim()),
                    Double.parseDouble(f[4].trim()),
                    Integer.parseInt(f[5].trim())));
        }
        return new Dataset(rows);
    }
}
