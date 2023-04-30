package benchmark;

public class RunBoth {
    public static void main(String[] args) throws Exception {
        BenchmarkFixedRate.main(args);
        BenchmarkFixedRateWithWarmup.main(args);
        new ProcessBuilder("shutdown", "-h", "now").start();
    }
}
