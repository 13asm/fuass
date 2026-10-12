package fuass.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses CSV files from the classpath resources.
 * Handles quoted fields that may contain commas.
 */
public class CSVReader {

    private CSVReader() {}

    public static List<String[]> readRows(String csvPath) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (InputStream is = CSVReader.class.getResourceAsStream("/" + csvPath)) {
            if (is == null) {
                throw new IOException("CSV file not found: " + csvPath);
            }
            try (BufferedReader br = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    rows.add(parseCsvLine(line));
                }
            }
        }
        return rows;
    }

    /**
     * Parses a single CSV line, handling quoted fields that may contain commas.
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}