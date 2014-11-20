import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * Created by Anders on 2014-11-10.
 */
public class Main {

    public Main() {
        JFrame frame = new JFrame("Hemligt program");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Screen screen = new Screen();
        frame.setContentPane(screen);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final Chip8 chip8 = new Chip8();
        String fileName = "games/PONG";
        int fileSize = chip8.loadGame(new File(fileName));
        System.out.printf("Read %s of size %d.%n", fileName, fileSize);

        Timer timer = new Timer(200, new ActionListener() { // should be set to 40
            @Override
            public void actionPerformed(ActionEvent e) {
                chip8.emulateCycle();
                if (chip8.isGfxUpdated()) {
                    chip8.drawToImage(screen.getImage());
                    screen.repaint();
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
