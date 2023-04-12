package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class BenchmarkFixedRate extends BenchmarkDelayRate {

    public BenchmarkFixedRate(String host, boolean storeResults) {
        super(host, storeResults);
    }

    public BenchmarkFixedRate() {
    }

    private final String baseFolder = "results/" + getClassName() + "/" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

    public Results run(ServerProcess process, Duration testDuration, int numberOfClients, double requestPerSecond) throws IOException, InterruptedException {
        if (super.baselineEnergyUJoules == 0) {
            measureBaseline();
        }
        double expected = numberOfClients * testDuration.toSeconds() * requestPerSecond;
        System.out.printf("Starting %s with %d clients and %.2f reqs/sec for %ds. Expected requests: %.2f %n", process.getName(), numberOfClients, requestPerSecond, testDuration.toSeconds(), expected);
        int maxThreads = 32;
        int minThreads = (int) Math.min(numberOfClients, numberOfClients * requestPerSecond);
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(Math.min(maxThreads, minThreads));
        FileWriter writer;
        FileWriter toClose = null;
        try (var ignored = getDatabaseProcess().start(); var ignored2 = process.start()) {
            if (super.writeResults) {
                File f = new File(baseFolder);
                if (!f.exists() && !f.mkdirs()) {
                    throw new RuntimeException("could not create folder: " + f);
                }
                writer = new FileWriter(new File(baseFolder, "%s-%ds-%d-%.2f.txt".formatted(process.getName(), testDuration.toSeconds(), numberOfClients, requestPerSecond)));
                toClose = writer;
            } else {
                writer = null;
            }
            List<ScheduledFuture<?>> futures = new ArrayList<>();
            RequestMaker maker = newRequestMaker(process);
            CPUSnapshot snap = cpuSnapshot();
            long startJoules = maker.energyMeasureMicroJoules();

            long period = (long) (1000 / requestPerSecond);
            long t0 = System.nanoTime();
            List<Duration> latencies = new CopyOnWriteArrayList<>();
            long deadline = System.nanoTime() + testDuration.toNanos();
            for (int i = 0; i < numberOfClients; i++) {
                var fut = pool.scheduleAtFixedRate(() -> {
                    if (System.nanoTime() <= deadline) {
                        try {
                            Duration e = maker.makeRequest();
                            if (System.nanoTime() <= deadline) {
                                latencies.add(e);
                                if (writer != null) writer.write(e.toMillis() + "\n");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 0, period, TimeUnit.MILLISECONDS);
                futures.add(fut);
            }
            TimeUnit.SECONDS.sleep(testDuration.toSeconds());
            for (var fut : futures) {
                fut.cancel(false);
//                latencies.addAll(fut.get());
            }
            for (var fut : futures) {
                try {
                    fut.get();
                } catch (CancellationException ignored1) {
                }
            }
            long totalEnergy = maker.energyMeasureMicroJoules() - startJoules;
            long totalTime = System.nanoTime() - t0;
            System.out.printf("Finished %s with %d requests%n", process.getName(), latencies.size());

            return new Results(process.getName(), latencies, totalEnergy, Duration.ofNanos(totalTime), cpuSnapshot().diffFrom(snap)).subtractBaseline(baselineEnergyUJoules, baselineMeasureDuration);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            pool.shutdownNow();
            if (toClose != null) toClose.close();
        }
    }

    public static void main(String[] args) throws IOException {
        BenchmarkFixedRate rate = new BenchmarkFixedRate("localhost", true);
//        rate.disableBaseline();
        List<String> procNames = Arrays.asList("pythonjude", "javaquarkus", "ruby", "golang", "node");
        Collections.shuffle(procNames);

        Duration testDuration = Duration.ofSeconds(600);
        List<RateInputParameters> list = new ArrayList<>();
        for (var proc : procNames) {
            int numberOfClients = 8;
            double reqsPerSecond = 1;
            while (numberOfClients >= 1) {
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
