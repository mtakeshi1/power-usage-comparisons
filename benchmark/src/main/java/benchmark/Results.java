package benchmark;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

public record Results(String name, List<Long> latenciesMillis, long energyUsageMicrojoules, Duration sampleDuration) {
    public Results {
        Collections.sort(latenciesMillis);
    }

    public Results subtractBaseline(long energyMicrojoules, Duration baselineDuration) {
        double baselinePowerUJsec = ((double) energyMicrojoules) / baselineDuration.toSeconds();
        long baselineEnergyUJ = (long) (sampleDuration.toSeconds() * baselinePowerUJsec);
        return new Results(name, latenciesMillis, this.energyUsageMicrojoules() - baselineEnergyUJ, sampleDuration);
    }

    public double average() {
        return latencies().average().orElse(0);
    }

    private LongStream latencies() {
        return latenciesMillis.stream().mapToLong(Long::longValue);
    }

    private double powerWatt() {
        double duration = sampleDuration.toSeconds();
        return (energyUsageMicrojoules / 1_000_000.0) / duration;
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
        return "%s, samples=%d, duration(secs)=%d, avg=%.2g, median=%d, p99=%d, power(joules)=%d, max: %d, power(w): %.2g"
                .formatted(name, latenciesMillis.size(), sampleDuration.toSeconds(), average(), median(), p99(), energyUsageMicrojoules / 1_000_000, max(), powerWatt());
    }
}
