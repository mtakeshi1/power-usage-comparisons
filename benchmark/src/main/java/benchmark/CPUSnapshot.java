package benchmark;

public record CPUSnapshot(long userTime, long system, long idle, long others) {

//    public static CPUSnapshot parseLinuxStat() {
//        try(var in = new BufferedReader(new FileReader("/proc/"))) {
//
//        } catch (IOException e) {
//
//        }
//        return new CPUSnapshot(0, 0, 1, 0);
//    }

    public static CPUSnapshot parseLinuxStat(String fullFile) {
        String[] lines = fullFile.split("\n");
        for (var line : lines) {
            if (line.startsWith("cpu ")) {
                String[] parts = fullFile.split(" +");
                long user = Long.parseLong(parts[1]) + Long.parseLong(parts[2]);
                long system = Long.parseLong(parts[3]);
                long idle = Long.parseLong(parts[4]);
                long others = Long.parseLong(parts[5]) + Long.parseLong(parts[6]) + Long.parseLong(parts[7]);
                return new CPUSnapshot(user, system, idle, others);
            }
        }
        /*
        user: normal processes executing in user mode
        nice: niced processes executing in user mode
        system: processes executing in kernel mode
        idle: twiddling thumbs
        iowait: waiting for I/O to complete
        irq: servicing interrupts
        softirq: servicing softirqs
         */
        return new CPUSnapshot(0, 0, 1, 0);
    }

    public CPUUsage diffFrom(CPUSnapshot previous) {
        return new CPUUsage(
                this.userTime - previous.userTime,
                this.system - previous.system,
                this.idle - previous.idle,
                this.others - previous.others
        );
    }

}
