package fuass.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    private CSVReader() {}

    public static List<String[]> readRows(String csvPath) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (InputStream is = CSVReader.class.getResourceAsStream("/" + csvPath)) {
            assert is != null;
            try (BufferedReader br = new BufferedReader(
                         new InputStreamReader(is, StandardCharsets.UTF_8))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    rows.add(line.split(",", -1));
                }
            }
        }
        return rows;
    }
}