package benchmark;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class BenchmarkBase {

    private final ServerProcess databaseProcess = standardDatabaseProcess();

    private final ServerProcess[] variations = {
            quarkus(STANDARD_PORT),

            ruby(STANDARD_PORT),
            golang(STANDARD_PORT),
            node(STANDARD_PORT),
            jude(STANDARD_PORT),
    };

    private final String host;

    private long baselineEnergyUJoules;
    private final Duration baselineMeasureDuration = Duration.ofMinutes(2);

    public BenchmarkBase(String host) {
        this.host = host;
        if (!host.equals("localhost")) {
            for (int i = 0; i < variations.length; i++) {
                variations[i] = variations[i].overSSH(host);
            }
        }
    }

    public BenchmarkBase() {
        this("localhost");
    }

    public static final int STANDARD_PORT = 8080;

    public ServerProcess getDatabaseProcess() {
        return databaseProcess;
    }

    public static ServerProcess standardDatabaseProcess() {
        return ServerProcess.dockerProcess("pgsql", "power/pgsql", ".env", 5432, 5432);
    }

    public static ServerProcess quarkus(int externalPort) {
        return ServerProcess.dockerProcess("javaquarkus", "power/java", ".env", externalPort, 8080);
    }

    public static ServerProcess jude(int externalPort) {
        return ServerProcess.dockerProcess("pythonjude", "power/jude", ".env", externalPort, 8000);
    }

    public static ServerProcess ruby(int externalPort) {
        return ServerProcess.dockerProcess("ruby", "power/ruby", ".env", externalPort, 8080);
    }

    public static ServerProcess golang(int externalPort) {
        return ServerProcess.dockerProcess("golang", "power/golang", ".env", externalPort, 8080);
    }

    public static ServerProcess node(int externalPort) {
        return ServerProcess.dockerProcess("node", "power/node", ".env", externalPort, 8080);
    }


    public void sanityAll() throws Exception {
        for (ServerProcess serverProcess : variations) {
            sanity(serverProcess);
        }
    }

    public void sanity(ServerProcess proc) throws Exception {
        System.out.println("starting " + proc.getName());
        try (var ignored = databaseProcess.start(); var ignored2 = proc.start()) {
            System.out.println(proc.getName() + "started");
            RequestMaker maker = newRequestMaker(proc);
            maker.makeRequest();
            System.out.println(proc.getName() + " all good");
        }
    }

    public void thousandRequests() throws Exception {
        for (var proc : variations) {
            thousandRequests(proc);
        }
    }

    public void measureBaseline() {
        RequestMaker maker = newRequestMaker("baseline");
        try {
            long initialJ = maker.energyMeasureMicroJoules();
            TimeUnit.NANOSECONDS.sleep(baselineMeasureDuration.toNanos());
            this.baselineEnergyUJoules = maker.energyMeasureMicroJoules() - initialJ;
        } catch (Exception e) {
            throw new RuntimeException("could not stabilish baseline energy usage", e);
        }
    }

    public void thousandRequests(ServerProcess proc) throws Exception {
        int requests = 1000;
        int pauseMillis = 10;
        System.out.println("starting " + proc.getName() + " with " + requests + " requests with pause: " + pauseMillis);
        try (var ignored = databaseProcess.start(); var ignored2 = proc.start()) {
            System.out.println(proc.getName() + " started");
            RequestMaker maker = newRequestMaker(proc);
            Results results = maker.loop(requests, Duration.ofMillis(pauseMillis)).subtractBaseline(this.baselineEnergyUJoules, this.baselineMeasureDuration);
            System.out.println(results);
        }
    }

    protected RequestMaker newRequestMaker(String procName) {
        return new RequestMaker(procName, host, STANDARD_PORT, STANDARD_PORT + 1);
    }


    protected RequestMaker newRequestMaker(ServerProcess proc) {
        return newRequestMaker(proc.getName());
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkBase().thousandRequests();
    }


}