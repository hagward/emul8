import javax.swing.*;
import java.awt.*;

/**
 * Created by Anders on 2014-11-08.
 */
public class Main {

    public static final int FRAMES_PER_SECOND = 25;
    public static final int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;

    private long startMilliSeconds;

    private long getTickCount() {
        return System.currentTimeMillis() - startMilliSeconds;
    }

    public Main() {
        JFrame frame = new JFrame("Chip8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Screen screen = new Screen();
        frame.setContentPane(screen);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final Chip8 chip8 = new Chip8();
        startMilliSeconds = System.currentTimeMillis();

        Runnable game = new Runnable() {
            @Override
            public void run() {
                long nextTick = 0;
                long sleepTime = 0;
                while (true) {
                    chip8.emulateCycle();
                    if (chip8.gfxUpdated) {
                        screen.draw(chip8.gfx);
                        chip8.gfxUpdated = false;
                    }

                    nextTick += SKIP_TICKS;
                    sleepTime = nextTick - getTickCount();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main();
            }
        });
    }
}
