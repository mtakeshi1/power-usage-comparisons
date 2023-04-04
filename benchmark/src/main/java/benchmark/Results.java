package benchmark;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

public record Results(String name, List<Long> latenciesMillis, long powerUsageMicrojoules, Duration sampleDuration) {
    public Results {
        Collections.sort(latenciesMillis);
    }

    public double average() {
        return latencies().average().orElse(0);
    }

    private LongStream latencies() {
        return latenciesMillis.stream().mapToLong(Long::longValue);
    }

    public long max() {
        return latencies().max().orElse(0);
    }

    public long percentile(double perc) {
        int index = (int) (perc * latenciesMillis.size());
        return latenciesMillis.get(index);
    }

    public long median() {
        return percentile(0.5);
    }

    public long p99() {
        return percentile(0.99);
    }

    @Override
    public String toString() {
        return "Results{name=%s, samples=%d, duration(secs)=%d, avg=%s, median=%d, p99=%d, power(joules)=%d}"
                .formatted(name, latenciesMillis.size(), sampleDuration.toSeconds(), average(), median(), p99(), powerUsageMicrojoules / 1_000_000);
    }
}
