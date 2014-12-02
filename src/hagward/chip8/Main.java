package hagward.chip8;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main {

    public static void printDebug(String s) {
        System.out.println("dbg: " + s);
    }

    public Main(String romFileName) {
        final String frameTitle = "EMUL8 - " + romFileName;
        final JFrame frame = new JFrame(frameTitle);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final Display display = new Display(Display.DISPLAY_WIDTH * Display.DISPLAY_MODIFIER,
                Display.DISPLAY_HEIGHT * Display.DISPLAY_MODIFIER);
        frame.setContentPane(display);

        frame.pack();
        frame.setLocationRelativeTo(null);

        final Keyboard keyboard = new Keyboard();
        final Chip8 chip8 = new Chip8(display, keyboard);
        final File romFile = new File(romFileName);
        int fileSize = chip8.loadRom(romFile);
        if (fileSize == -1) {
            System.exit(1);
        }
        System.out.printf("Read %s of size %d bytes.%n", romFileName, fileSize);

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);

                case KeyEvent.VK_P:
                    // TODO: Pause/resume the emulation.
                    if (chip8.isRunning()) {
                        chip8.stop();
                        frame.setTitle(frameTitle + " (paused)");
                    } else {
                        chip8.start();
                        frame.setTitle(frameTitle);
                    }
                    break;

                case KeyEvent.VK_BACK_SPACE:
                    chip8.reset();
                    chip8.loadRom(romFile);
                    break;

                default:
                    chip8.setKey(e.getKeyChar(), true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                chip8.setKey(e.getKeyChar(), false);
            }
        });

        frame.setVisible(true);
        chip8.start();
    }

    public static void main(String[] args) {
        args = new String[] { "", "roms/INVADERS" };
        if (args.length != 2) {
            System.out.println("Usage: emul8 <rom>");
        } else {
            new Main(args[1]);
        }
    }
}
