package benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BenchmarkMaxRate extends BenchmarkBase implements Benchmark<MaxDuration> {
    public BenchmarkMaxRate(String host) {
        super(host);
    }

    public BenchmarkMaxRate() {
    }

    @Override
    public Results run(MaxDuration input) {
        if (super.baselineEnergyUJoules == 0) {
            measureBaseline();
        }
        log("Starting %s with %d clients and for %ds".formatted(input.imageName(), input.numClients(), input.duration().toSeconds()));
        var process = selectProcess(input.imageName());
        RunningProcess runningProcess = null;
        ExecutorService executor = Executors.newFixedThreadPool(input.numClients());
        try (var ignored = getDatabaseProcess().start(); var server = process.start()) {
            runningProcess = server;
            List<Future<List<Duration>>> futures = new ArrayList<>();
            RequestMaker maker = newRequestMaker(process);
            CPUSnapshot snap = cpuSnapshot();
            long startJoules = maker.energyMeasureMicroJoules();
            long deadline = System.nanoTime() + input.duration().toNanos();
            long t0 = System.nanoTime();
            for (int i = 0; i < input.numClients(); i++) {
                futures.add(executor.submit(() -> {
                    List<Duration> list = new ArrayList<>();
                    while (System.nanoTime() < deadline) {
                        list.add(maker.makeRequest());
                    }
                    return list;
                }));
            }
            List<Duration> all = new ArrayList<>();
            for (var fut : futures) {
                try {
                    all.addAll(fut.get());
                } catch (CancellationException ignored1) {
                } catch (ExecutionException e) {
                    System.err.println(server.stdout());
                    throw new RuntimeException(e.getCause());
                }
            }
            long totalEnergy = maker.energyMeasureMicroJoules() - startJoules;
            long totalTime = System.nanoTime() - t0;
            Results results = new Results(process.getName(), all, totalEnergy, Duration.ofNanos(totalTime), cpuSnapshot().diffFrom(snap)).subtractBaseline(baselineEnergyUJoules, baselineMeasureDuration);
            log("Finished %s with %d requests and results: %s%n".formatted(process.getName(), all.size(), results));

            return results;
        } catch (Exception e) {
            log("error runing: %s - message: %s".formatted(input.imageName(), e.getMessage()));
            if (runningProcess != null) {
                log(runningProcess.stdout().toString());
            }
        } finally {
            executor.shutdownNow();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        var max = new BenchmarkMaxRate(args.length > 0 ? args[0] : "localhost");
        Duration duration = Duration.ofSeconds(120);
        List<MaxDuration> list = new ArrayList<>();
        for (var image : max.allProcessNames()) {
            list.add(new MaxDuration(image, duration, 1));
            list.add(new MaxDuration(image, duration, 4));
        }
        max.runAllWriteResults(list);
    }
}
