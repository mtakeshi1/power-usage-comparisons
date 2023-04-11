package benchmark;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BenchmarkDelayRate extends BenchmarkBase implements Benchmark<RateInputParameters> {

    private final String baseFolder = "results/" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    protected final boolean writeResults;

    public BenchmarkDelayRate(String host, boolean writeAllResults) {
        super(host);
        this.writeResults = writeAllResults;
    }

    public BenchmarkDelayRate() {
        this("localhost", false);
    }

    public Map<String, Results> runAll(Duration testDuration, int numberOfClients, double requestPerSecond) throws Exception {
        measureBaseline();
        Map<String, Results> results = new HashMap<>();
        for (var proc : super.variations) {
            Results r = run(proc, testDuration, numberOfClients, requestPerSecond);
            System.out.println(r);
            results.put(proc.getName(), r);
        }
        return results;
    }

    public Results run(String procName, Duration testDuration, int numberOfClients, double requestPerSecond) throws IOException, InterruptedException {
        for (var proc : variations) {
            if (proc.getName().equals(procName)) {
                return run(proc, testDuration, numberOfClients, requestPerSecond);
            }
        }
        return null;
    }

    public Results run(ServerProcess process, Duration testDuration, int numberOfClients, double requestPerSecond) throws IOException, InterruptedException {
        if (super.baselineEnergyUJoules == 0) {
            measureBaseline();
        }
        FileWriter writer = null;
        try {
            System.out.printf("Starting %s with %d clients and %.2g reqs/sec for %ds%n", process.getName(), numberOfClients, requestPerSecond, testDuration.toSeconds());
            ExecutorService pool = Executors.newFixedThreadPool(numberOfClients);
            try (var ignored = getDatabaseProcess().start(); var ignored2 = process.start()) {
                List<Future<List<Duration>>> futures = new ArrayList<>(numberOfClients);
                RequestMaker maker = newRequestMaker(process);
                long startJoules = maker.energyMeasureMicroJoules();
                long deadLine = System.currentTimeMillis() + testDuration.toMillis();
                long period = (long) (1000 / requestPerSecond);
                long t0 = System.nanoTime();
                for (int i = 0; i < numberOfClients; i++) {
                    futures.add(pool.submit(() -> requestLoop(maker, deadLine, period)));
                }
                List<Duration> latencies = new ArrayList<>();
                for (var fut : futures) {
                    latencies.addAll(fut.get());
                }
                long totalEnergy = maker.energyMeasureMicroJoules() - startJoules;
                long totalTime = System.nanoTime() - t0;
                System.out.printf("Finished %s with %d requests%n", process.getName(), latencies.size());
                return new Results(process.getName(), latencies, totalEnergy, Duration.ofNanos(totalTime)).subtractBaseline(baselineEnergyUJoules, baselineMeasureDuration);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } finally {
                pool.shutdownNow();
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    protected List<Duration> requestLoop(RequestMaker maker, long deadlineMillis, long periodMillis) throws IOException, InterruptedException {
        List<Duration> list = new ArrayList<>();
        while (System.currentTimeMillis() < deadlineMillis) {
            long nextDeadline = System.currentTimeMillis() + periodMillis;
            Duration e = maker.makeRequest();
            list.add(e);
            long pause = System.currentTimeMillis() - nextDeadline;
            if (pause > 0) {
                Thread.sleep(pause);
            }
        }
        return list;
    }

    @Override
    public Results run(RateInputParameters input) {
        try {
            return run(input.imageName(), input.testDuration(), input.clients(), input.requestsPerSecond());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        BenchmarkDelayRate rate = new BenchmarkDelayRate();
        rate.disableBaseline();
        String[] procNames = {"pythonjude", "javaquarkus"};
        Duration testDuration = Duration.ofSeconds(60);
        List<RateInputParameters> list = new ArrayList<>();
        for (var proc : procNames) {
            int numberOfClients = 8;
            double reqsPerSecond = 1;
            while (numberOfClients > 0) {
                list.add(new RateInputParameters(proc, testDuration, numberOfClients, reqsPerSecond));
                numberOfClients /= 2;
                reqsPerSecond *= 2.0;
            }
        }
        Collections.reverse(list);
        String output = rate.runAllWriteResults(list);
        System.out.println(output);
    }

}
