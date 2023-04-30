package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FixedRequestsBenchmark extends BenchmarkBase implements Benchmark<FixedRequests> {

    public Results run(ServerProcess process, Duration testDuration, int numberOfClients, double requestPerSecond) throws IOException, InterruptedException {
//        if (super.baselineEnergyUJoules == 0) {
//            measureBaseline();
//        }
//        double expected = numberOfClients * testDuration.toSeconds() * requestPerSecond;
//        System.out.printf("Starting %s with %d clients and %.2f reqs/sec for %ds. Expected requests: %.2f %n", process.getName(), numberOfClients, requestPerSecond, testDuration.toSeconds(), expected);
//        int maxThreads = 32;
//        int minThreads = (int) Math.min(numberOfClients, numberOfClients * requestPerSecond);
//        ScheduledExecutorService pool = Executors.newScheduledThreadPool(Math.min(maxThreads, minThreads));
//        FileWriter writer;
//        FileWriter toClose = null;
//        try (var ignored = getDatabaseProcess().start(); var ignored2 = process.start()) {
//            if (writeResults) {
//                File f = getBaseFolder();
//                if (!f.exists() && !f.mkdirs()) {
//                    throw new RuntimeException("could not create folder: " + f);
//                }
//                writer = new FileWriter(new File(f, "%s-%ds-%d-%.2f.txt".formatted(process.getName(), testDuration.toSeconds(), numberOfClients, requestPerSecond)));
//                toClose = writer;
//            } else {
//                writer = null;
//            }
//            List<ScheduledFuture<?>> futures = new ArrayList<>();
//            RequestMaker maker = newRequestMaker(process);
//            beforeRunStart(maker, process);
//            CPUSnapshot snap = cpuSnapshot();
//            long startJoules = maker.energyMeasureMicroJoules();
//
//            long period = (long) (1000 / requestPerSecond);
//            long t0 = System.nanoTime();
//            List<Duration> latencies = new CopyOnWriteArrayList<>();
//            long deadline = System.nanoTime() + testDuration.toNanos();
//            for (int i = 0; i < numberOfClients; i++) {
//                futures.add(scheduleTask(pool, writer, maker, period, latencies, deadline));
//            }
//            TimeUnit.SECONDS.sleep(testDuration.toSeconds());
//            for (var fut : futures) {
//                fut.cancel(false);
////                latencies.addAll(fut.get());
//            }
//            for (var fut : futures) {
//                try {
//                    fut.get();
//                } catch (CancellationException ignored1) {
//                }
//            }
//            long totalEnergy = maker.energyMeasureMicroJoules() - startJoules;
//            long totalTime = System.nanoTime() - t0;
//            Results results = new Results(process.getName(), latencies, totalEnergy, Duration.ofNanos(totalTime), cpuSnapshot().diffFrom(snap)).subtractBaseline(baselineEnergyUJoules, baselineMeasureDuration);
//            System.out.printf("Finished %s with %d requests and results: %s%n", process.getName(), latencies.size(), results);
//
//            return results;
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e.getCause());
//        } finally {
//            pool.shutdownNow();
//            if (toClose != null) toClose.close();
//        }
        throw new RuntimeException();
    }

    @Override
    public Results run(FixedRequests input) {
        if (super.baselineEnergyUJoules == 0) {
            measureBaseline();
        }
        ScheduledExecutorService service = Executors.newScheduledThreadPool(input.threads() + 1);
        try {

        } finally {
            service.shutdownNow();
        }

        return null;
    }

    @Override
    public File getBaseFolder() {
        return null;
    }
}
