package benchmark;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BenchmarkFixedRateWithWarmup extends BenchmarkFixedRate {

    public BenchmarkFixedRateWithWarmup(String host, boolean storeResults) {
        super(host, storeResults);
    }

    public BenchmarkFixedRateWithWarmup() {
    }

    protected void beforeRunStart(RequestMaker maker, ServerProcess process) throws IOException, InterruptedException {
        System.out.println("starting warmup for: " + process.getName());
        for (int i = 0; i < 1000; i++) {
            maker.makeRequest();
        }
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        BenchmarkFixedRateWithWarmup rate = new BenchmarkFixedRateWithWarmup(args.length == 0 ? "localhost" : args[0], false);
//        rate.disableBaseline();
        rate.redirectOutputs();
        List<String> procNames = rate.allProcessNames();
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
