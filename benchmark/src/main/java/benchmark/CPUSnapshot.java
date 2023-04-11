package benchmark;

public record CPUSnapshot(long userTime, long system, long idle, long others) {

    public static CPUSnapshot parseLinuxStat(String line) {
        /*
        user: normal processes executing in user mode
        nice: niced processes executing in user mode
        system: processes executing in kernel mode
        idle: twiddling thumbs
        iowait: waiting for I/O to complete
        irq: servicing interrupts
        softirq: servicing softirqs
         */
        String[] parts = line.split(" ");
        long user = Long.parseLong(parts[1]) + Long.parseLong(parts[2]);
        long system = Long.parseLong(parts[3]);
        long idle = Long.parseLong(parts[4]);
        long others = Long.parseLong(parts[5]) + Long.parseLong(parts[6]) + Long.parseLong(parts[7]);
        return new CPUSnapshot(user, system, idle, others);
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
