package benchmark;

import java.io.Closeable;
import java.io.IOException;

public record RunningProcess(Process process, StringBuffer stdout, Runnable closeCommand) implements Closeable {

    public RunningProcess(Process process, Runnable closeCommand) {
        this(process, new StringBuffer(), closeCommand);
    }

    public RunningProcess {
        Thread t = new Thread() {
            final byte[] buffer = new byte[8 * 1024];

            @Override
            public void run() {
                try {
                    while (process.isAlive()) {
                        int x = copyOnce();
                        if (x == 0) {
                            Thread.sleep(10);
                        }
                    }
                    copyOnce();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private int copyOnce() throws IOException {
                int x = 0;
                int r = process.getInputStream().read(buffer);
                if (r > 0) stdout.append(new String(buffer, 0, r));
                x += r;
                r = process.getErrorStream().read(buffer);
                if (r > 0) System.err.write(buffer, 0, r);
                x += r;
                return x;
            }
        };
        t.setDaemon(true);
        t.start();

    }

    @Override
    public void close() {
        closeCommand.run();
    }
}
