package benchmark;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessHelper {

    public static ProcessOutput start(String[] stopCommands) throws IOException {
        return register(new ProcessBuilder(stopCommands).start());
    }

    record ProcessOutput(StringBuffer stdout, StringBuffer stderr, Process process) {
        public ProcessOutput(Process process) {
            this(new StringBuffer(), new StringBuffer(), process);
        }

        public synchronized void waitForStdout() throws InterruptedException {
            int c = stdout.length();
            while (stdout.length() == c) {
                wait(100);
            }
        }

        public void waitForStderr() throws InterruptedException {
            int c = stderr.length();
            while (stderr.length() == c) {
                wait(100);
            }

        }

        public void waitForAny() throws InterruptedException {
            int out = stdout.length();
            int err = stderr.length();
            while (stdout.length() == out && stderr.length() == err) {
                wait(100);
            }
        }

    }

    private static final Map<Process, ProcessOutput> PROCESSES = new ConcurrentHashMap<>();

    private static final byte[] BUFFER = new byte[8 * 1024];

    private static int copy(Process proc, ProcessOutput out) {
        int read = 0;
        try {
            if (proc.getInputStream().available() > 0) {
                int n = proc.getInputStream().read(BUFFER);
                if (n > 0) {
                    read += n;
                    out.stdout().append(new String(BUFFER, 0, n));
                }
            }
            if (proc.getErrorStream().available() > 0) {
                int n = proc.getErrorStream().read(BUFFER);
                if (n > 0) {
                    read += n;
                    out.stderr().append(new String(BUFFER, 0, n));
                }
            }
            synchronized (out) {
                out.notifyAll();
            }
            if (!proc.isAlive()) {
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read;
    }

    private static final Thread COPIER = new Thread() {
        @Override
        public void run() {
            while (true) {
                int c = 0;
                for (var entry : PROCESSES.entrySet()) {
                    int n = copy(entry.getKey(), entry.getValue());
                    if (n == -1) {
                        PROCESSES.remove(entry.getKey(), entry.getValue());
                    } else c += n;
                }
                if (c == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    };

    static {
        COPIER.setDaemon(true);
        COPIER.start();
    }

    public static ProcessOutput register(Process process) {
        ProcessOutput out = new ProcessOutput(process);
        PROCESSES.put(process, out);
        return out;
    }

}
