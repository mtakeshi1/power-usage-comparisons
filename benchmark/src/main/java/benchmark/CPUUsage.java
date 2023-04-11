package benchmark;

public record CPUUsage(double userTime, double system, double idle, double others) {

    public double total() {
        return userTime + system + idle + others;
    }

    public double totalCPUUsagePercentage() {
        double used = userTime + system + others;
        return used / total();
    }

    public double userPercentage() {
        return userTime / total();
    }

    public double systemPercentage() {
        return system / total();
    }

}
