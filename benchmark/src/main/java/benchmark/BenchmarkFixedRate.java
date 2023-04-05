package benchmark;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BenchmarkFixedRate extends BenchmarkBase {
    public BenchmarkFixedRate(String host) {
        super(host);
    }

    public BenchmarkFixedRate() {
    }

//    public Results runAll(Duration testDuration, int numberOfClients, double requestPerSecond) throws Exception {
//
//    }

    public Results run(ServerProcess process, Duration testDuration, int numberOfClients, double requestPerSecond) throws Exception {
        measureBaseline();
        ExecutorService pool = Executors.newFixedThreadPool(numberOfClients);
        try (var ignored = getDatabaseProcess().start(); var ignored2 = process.start()) {
            List<Future<List<Long>>> futures = new ArrayList<>(numberOfClients);
            RequestMaker maker = newRequestMaker(process);
            long startJoules = maker.energyMeasureMicroJoules();
            long deadLine = System.currentTimeMillis() + testDuration.toMillis();
            long period = (long) (1000 / requestPerSecond);
            long t0 = System.nanoTime();
            for(int i = 0; i < numberOfClients; i++) {
                futures.add(pool.submit(() -> requestLoop(maker, deadLine, period)));
            }
            List<Long> latencies = new ArrayList<>();
            for(var fut : futures) {
                latencies.addAll(fut.get());
            }
            long totalEnergy = maker.energyMeasureMicroJoules() - startJoules;
            long totalTime = System.nanoTime() - t0;
            return new Results(process.getName(), latencies, totalEnergy, Duration.ofNanos(totalTime));
        } finally {
            pool.shutdownNow();
        }
    }

    private List<Long> requestLoop(RequestMaker maker, long deadlineMillis, long periodMillis) throws IOException, InterruptedException {
        List<Long> list = new ArrayList<>();
        while (System.currentTimeMillis() < deadlineMillis) {
            long nextDeadline = System.currentTimeMillis() + periodMillis;
            list.add(maker.makeRequest());
            long pause = System.currentTimeMillis() - nextDeadline;
            if(pause > 0) {
                Thread.sleep(pause);
            }
        }
        return list;
    }

}
