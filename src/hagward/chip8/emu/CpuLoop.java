package hagward.chip8.emu;

public class CpuLoop implements Runnable {

    private final Cpu cpu;

    public CpuLoop(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public void run() {
        try {
            while (cpu.isRunning()) {
                cpu.emulateCycle();
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Who interrupted this poor thread?");
        }
    }

}
