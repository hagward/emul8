package hagward.chip8.emu;

import hagward.chip8.gui.Display;

import java.io.File;
import java.io.FileInputStream;

public class Chip8 {

    private final Keyboard keyboard;

    private final Cpu cpu;
    private final Memory memory;
    private final Video video;

    private File romFile;

    public Chip8(Display display, Keyboard keyboard) {
        this.keyboard = keyboard;

        memory = new Memory();
        video = new Video(display);
        cpu = new Cpu(memory, video, keyboard);
        romFile = null;
    }

    public void reset() {
        cpu.reset();
        memory.reset();
        video.clear();
        video.flushVideo();
    }

    public void start() {
        cpu.start();
    }

    public void stop() {
        cpu.stop();
    }

    public boolean isRunning() {
        return cpu.isRunning();
    }

    public int loadRom(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            int nextByte;
            int i = 0x200;
            while ((nextByte = in.read()) != -1) {
                memory.setByte(i++, nextByte);
            }
            in.close();
            romFile = file;
            return i - 0x200;
        } catch (Exception e) {
            System.out.println("Error loading rom: " + e.getMessage());
            return -1;
        }
    }

    public File getRom() {
        return romFile;
    }

    public void setKey(char keyChar, boolean pressed) {
        keyboard.setKey(keyChar, pressed);
    }

}
