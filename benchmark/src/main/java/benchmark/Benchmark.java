package benchmark;

import java.io.*;
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
            try {
                var res = run(in);
                rows.add(append(in.toCSV(), res.toCSV()));
            } catch (Exception e) {
                System.err.println("Error running " + in);
                e.printStackTrace();
            }
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
        File f = getBaseFolder();
        if (!f.exists() && !f.mkdirs()) {
            throw new RuntimeException("could not create folder: " + f);
        }
//        String className = getClassName();
        File out = new File(f, "results.csv");
        System.out.println("writing to file: " + out.getAbsolutePath());
        try (var fout = new BufferedWriter(new FileWriter(out))) {
            fout.write(csv);
        }
        return csv;
    }

    class TeeOutputStream extends OutputStream {

        private final OutputStream one;

        private final OutputStream two;

        public TeeOutputStream(OutputStream one, OutputStream two) {
            this.one = one;
            this.two = two;
        }


        @Override
        public void write(byte[] b) throws IOException {
            one.write(b);
            two.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            one.write(b, off, len);
            two.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            one.write(b);
            two.write(b);
        }

        @Override
        public void flush() throws IOException {
            one.flush();
            two.flush();
        }

        @Override
        public void close() throws IOException {
            try {
                one.close();
            } finally {
                two.close();
            }
        }
    }

    default PrintStream combineAutoFlush(OutputStream one, OutputStream two) {
        return new PrintStream(new TeeOutputStream(one, two), true);
    }

    default void redirectOutputs() throws IOException {
        File f = getBaseFolder();
        if (!f.exists() && !f.mkdirs()) {
            throw new RuntimeException("could not create folder: " + f);
        }
        System.setOut(combineAutoFlush(System.out, new FileOutputStream(new File(f, "stdout.log"))));
        System.setErr(combineAutoFlush(System.err, new FileOutputStream(new File(f, "stderr.log"))));
    }

    File getBaseFolder();

    default String getClassName() {
        String className = getClass().getName();
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        return className;
    }

}
