package hagward.chip8;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class Main {

    public Main() {
        JFrame frame = new JFrame("Hemligt program");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final Screen screen = new Screen();
        frame.setContentPane(screen);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final Chip8 chip8 = new Chip8();
        String fileName = "tests/invaders.c8";
        int fileSize = chip8.loadGame(new File(fileName));
        System.out.printf("Read %s of size %d bytes.%n", fileName, fileSize);

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                chip8.keyPress(e.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                chip8.keyRelease(e.getKeyChar());
            }
        });

        Timer timer = new Timer(40, e -> {
            chip8.emulateCycle();
            if (chip8.isGfxUpdated()) {
                chip8.drawToImage(screen.getImage());
                screen.repaint();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
