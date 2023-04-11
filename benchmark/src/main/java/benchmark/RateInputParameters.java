package benchmark;

import java.time.Duration;

public record RateInputParameters(String imageName, Duration testDuration, int clients, double requestsPerSecond) implements InputParameters {
    @Override
    public String[] headers() {
        return new String[]{"name", "duration(secs)", "num_clients", "reqs/s"};
    }

    @Override
    public String[] toCSV() {
        return new String[]{imageName, String.valueOf(testDuration.toSeconds()), String.valueOf(clients), numberFormat().format(requestsPerSecond)};
    }
}
