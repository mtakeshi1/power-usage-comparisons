package benchmark;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BenchmarkFixedRate extends BenchmarkBase {
    public BenchmarkFixedRate(String host) {
        super(host);
    }

    public BenchmarkFixedRate() {
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

    public Results run(ServerProcess process, Duration testDuration, int numberOfClients, double requestPerSecond) throws Exception {
        if (super.baselineEnergyUJoules == 0) {
            measureBaseline();
        }
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
            return new Results(process.getName(), latencies, totalEnergy, Duration.ofNanos(totalTime)).subtractBaseline(baselineEnergyUJoules, baselineMeasureDuration);
        } finally {
            pool.shutdownNow();
        }
    }

    private List<Duration> requestLoop(RequestMaker maker, long deadlineMillis, long periodMillis) throws IOException, InterruptedException {
        List<Duration> list = new ArrayList<>();
        while (System.currentTimeMillis() < deadlineMillis) {
            long nextDeadline = System.currentTimeMillis() + periodMillis;
            list.add(maker.makeRequest());
            long pause = System.currentTimeMillis() - nextDeadline;
            if (pause > 0) {
                Thread.sleep(pause);
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        BenchmarkFixedRate rate = new BenchmarkFixedRate();
        rate.runAll(Duration.ofMinutes(2), 1, 5);
    }

}
