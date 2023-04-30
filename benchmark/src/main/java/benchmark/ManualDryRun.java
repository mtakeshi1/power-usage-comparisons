package benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ManualDryRun {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        BenchmarkFixedRate rate = new BenchmarkFixedRate(args.length == 0 ? "localhost" : args[0], false);
        rate.disableBaseline();
        rate.redirectOutputs();
        List<String> procNames = rate.allProcessNames();
        Duration testDuration = Duration.ofSeconds(30);
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
        Collections.reverse(list);
        rate.runAllWriteResults(list);
    }
}
