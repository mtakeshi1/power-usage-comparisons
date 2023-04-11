package benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Benchmark<IN extends InputParameters> {

    Results run(IN input);

    default List<Results> runAll(List<IN> inputs) {
        return inputs.stream().map(this::run).toList();
    }

    default String runFormatCSV(List<IN> inputs) {
        String[] header = append(inputs.get(0).headers(), Results.header());
        List<String[]> rows = new ArrayList<>();
        for (var in : inputs) {
            var res = run(in);
            rows.add(append(in.toCSV(), res.toCSV()));
        }
        return formatCSV(header, rows);
    }


    default String formatCSV(String[] header, List<String[]> rows) {
        int[] maxCols = Arrays.stream(header).mapToInt(String::length).toArray();
        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                maxCols[i] = Math.max(maxCols[i], row[i].length());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < header.length; i++) {
            String h = header[i];
            sb.append(" ".repeat(maxCols[i] - h.length())).append(h);
            if (i < header.length - 1) sb.append(",");
        }
        sb.append("\n");
        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                String h = row[i];
                sb.append(" ".repeat(maxCols[i] - h.length())).append(h);
                if (i < row.length - 1) sb.append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static String[] append(String[] from, String[] to) {
        String[] array = new String[from.length + to.length];
        System.arraycopy(from, 0, array, 0, from.length);
        System.arraycopy(to, 0, array, from.length, to.length);
        return array;
    }

    default String runAllWriteResults(List<IN> inputs) throws IOException {
        String csv = runFormatCSV(inputs);
        String className = getClassName();
        String baseFolder = "results/";
        File f = new File(baseFolder);
        if (!f.exists() && !f.mkdirs()) {
            throw new RuntimeException("could not create folder: " + f);
        }
        File out = new File(f, className + "-" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + ".csv");
        System.out.println("writing to file: " + out.getAbsolutePath());
        try (var fout = new BufferedWriter(new FileWriter(out))) {
            fout.write(csv);
        }
        return csv;
    }

    default String getClassName() {
        String className = getClass().getName();
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        return className;
    }

}
