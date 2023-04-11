package benchmark;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.LongStream;

public record Results(String name, List<Duration> latencies, long energyUsageMicrojoules, Duration sampleDuration) {
    public Results {
        Collections.sort(latencies);
    }

    public Results subtractBaseline(long energyMicrojoules, Duration baselineDuration) {
        double baselinePowerUJsec = ((double) energyMicrojoules) / baselineDuration.toSeconds();
        long baselineEnergyUJ = (long) (sampleDuration.toSeconds() * baselinePowerUJsec);
        return new Results(name, latencies, this.energyUsageMicrojoules() - baselineEnergyUJ, sampleDuration);
    }

    public double average() {
        return latenciesMillis().average().orElse(0);
    }

    private LongStream latenciesMillis() {
        return latencies.stream().mapToLong(Duration::toMillis);
    }

    public double powerWatt() {
        double duration = sampleDuration.toSeconds();
        return (energyUsageMicrojoules / 1_000_000.0) / duration;
    }

    public double energyJoules() {
        return energyUsageMicrojoules / 1_000_000.0;
    }

    public long max() {
        return latenciesMillis().max().orElse(0);
    }

    public long percentile(double perc) {
        int index = (int) (perc * latencies.size());
        return latencies.get(index).toMillis();
    }

    public long median() {
        return percentile(0.5);
    }

    public long p99() {
        return percentile(0.99);
    }

    @Override
    public String toString() {
        return "%s, samples=%d, duration(secs)=%d, avg latency=%.2g, median latency=%d, p99 latency=%d, max latency: %d, energy(joules)=%g, avg_power_draw(w): %.2g, joules per request: %.2g"
                .formatted(name, latencies.size(), sampleDuration.toSeconds(), average(), median(), p99(),
                        max(), energyJoules(), powerWatt(), joulesPerRequest());
    }

    public double joulesPerRequest() {
        return energyJoules() / latencies.size();
    }

    public static String[] header() {
        return "samples,duration,avg_lat,median_lat,p99_lat,max_lat,energy(J),avg_power(W),energy_per_req".split(",");
    }

    public String[] toCSV() {
        NumberFormat instance = NumberFormat.getInstance(Locale.ROOT);
        instance.setMaximumFractionDigits(2);
        instance.setGroupingUsed(false);

        return new String[]{
                String.valueOf(latencies.size()), String.valueOf(sampleDuration.toSeconds()), instance.format(average()), String.valueOf(median()),
                String.valueOf(p99()), String.valueOf(max()), instance.format(energyJoules()), instance.format(energyJoules()), instance.format(joulesPerRequest())
        };

    }

}
