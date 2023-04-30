package benchmark;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BenchmarkFixedRate extends BenchmarkDelayRate {

    public BenchmarkFixedRate(String host, boolean storeResults) {
        super(host, storeResults);
    }

    public BenchmarkFixedRate() {
    }

    @Override
    protected ScheduledFuture<?> scheduleTask(ScheduledExecutorService pool, FileWriter writer, RequestMaker maker, long period, List<Duration> latencies, long deadline) {
        return pool.scheduleAtFixedRate(() -> {
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

    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        BenchmarkFixedRate rate = new BenchmarkFixedRate(args.length == 0 ? "localhost" : args[0], false);
        rate.disableBaseline();
        rate.redirectOutputs();
        List<String> procNames = List.of("scala"); //rate.allProcessNames();
        Duration testDuration = Duration.ofSeconds(120);
        List<RateInputParameters> list = new ArrayList<>();
        for (var proc : procNames) {
            int numberOfClients = 8;
            double reqsPerSecond = 1;
            while (numberOfClients >= 1) {
                list.add(new RateInputParameters(proc, testDuration, numberOfClients, reqsPerSecond));
                numberOfClients /= 2;
                reqsPerSecond *= 2.0;
            }
//            numberOfClients = 16;
//            reqsPerSecond = 1;
//            while (numberOfClients >= 1) {
//                list.add(new RateInputParameters(proc, testDuration, numberOfClients, reqsPerSecond));
//                numberOfClients /= 2;
//                reqsPerSecond *= 2.0;
//            }
        }
        rate.runAllWriteResults(list);
    }

}
