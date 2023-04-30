package benchmark;

import java.time.Duration;

public record FixedRequests(String imageName, int threads, Duration testDuration, Duration reportPeriod) implements InputParameters {
    @Override
    public String[] headers() {
        return new String[]{"name", "threads", "test_duration"};
    }

    @Override
    public String[] toCSV() {
        return new String[]{imageName, String.valueOf(threads), String.valueOf(testDuration.toSeconds())};
    }
}
