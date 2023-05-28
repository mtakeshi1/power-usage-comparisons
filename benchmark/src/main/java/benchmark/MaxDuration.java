package benchmark;

import java.time.Duration;

public record MaxDuration(String imageName, Duration duration, int numClients) implements InputParameters {
    @Override
    public String[] headers() {
        return new String[]{"image", "duration", "numClients"};
    }

    @Override
    public String[] toCSV() {
        return new String[]{imageName, String.valueOf(duration.toSeconds()), String.valueOf(numClients)};
    }
}
