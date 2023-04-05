package benchmark;

import java.io.Closeable;
import java.io.IOException;

public class ServerProcess {

    private final String name;
    private volatile Process process;

    private final String[] startCommands;
    private final String[] stopCommands;


    public static ServerProcess dockerProcess(String name, String image, String envFile, int externalPort, int internalPort) {
        String[] start = {"docker", "run", "-d", "--rm", "--name", name, "-p%d:%d".formatted(externalPort, internalPort), "--env-file", envFile, image};
        String[] stop = {"docker", "stop", name};
        return new ServerProcess(name, start, stop);
    }

    public ServerProcess(String name, String[] startCommands, String[] stopCommands) {
        this.name = name;
        this.startCommands = startCommands;
        this.stopCommands = stopCommands;
    }

    public ServerProcess(String name, String[] startCommands) {
        this(name, startCommands, null);
    }

    public synchronized Closeable start() throws IOException, InterruptedException {
        if (process != null && process.isAlive()) {
            stop();
        }
        ProcessBuilder pb = new ProcessBuilder(startCommands);
        this.process = pb.start();
        ProcessHelper.register(this.process);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    ServerProcess.this.stop();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread.sleep(5_000);
        return () -> {
            try {
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
    }

    public String getName() {
        return name;
    }

    public synchronized void stop() throws IOException, InterruptedException {
        if (stopCommands != null) {
            // we will call the stop command even if no process has been found
            Process stopCmd = ProcessHelper.start(stopCommands).process();
            stopCmd.waitFor();
        }
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    public boolean isDocker() {
        return startCommands[0].endsWith("docker");
    }

    public ServerProcess overSSH(String host) {
        if (isDocker()) {
            return new ServerProcess(this.name, dockerOverSSH(startCommands, host), dockerOverSSH(stopCommands, host));
        } else {
            return new ServerProcess(this.name, prependSSH(startCommands, host), prependSSH(stopCommands, host));
        }
    }

    private static String[] dockerOverSSH(String[] original, String host) {
        if (original == null) {
            return null;
        }
        String[] r = new String[2 + original.length];
        r[0] = "docker";
        r[1] = "-H";
        r[2] = "ssh://" + host;
        System.arraycopy(original, 1, r, 3, original.length-1);
        return r;
    }

    private static String[] prependSSH(String[] original, String host) {
        if (original == null) {
            return null;
        }
        String[] r = new String[2 + original.length];
        r[0] = "ssh";
        r[1] = host;
        System.arraycopy(original, 0, r, 2, original.length);
        return r;
    }

}
