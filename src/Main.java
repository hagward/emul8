import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

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
        JFrame frame = new JFrame("Hemligt program");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Screen screen = new Screen();
        frame.setContentPane(screen);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final Chip8 chip8 = new Chip8();
        String fileName = "BREAKOUT";
        int fileSize = chip8.loadGame(
                "C2A20DE100A600B6C0C6F3D6C046B156002A8EAD6BCD6DE600222D6610861006" +
                "080F510F70030021E17C71778096FF2AEE6D172A8EAD6BCD6D06100E1AB7EF06" +
                "400E1AB72006F1B820AD6B06C00E1AD7EF06D00E1AD72006F1D820CD6D2AEE6D" +
                "176848784906F3682016F17821640021C764F3216874F196FF740096106D1721" +
                "E2861036100807085B21E886FF36A00807085DF310216A16200851F31021EB08" +
                "51F31021CC0851F310216C06020F81222DE843222D66E33310661086FF331086" +
                "1021A197FF94EF96FF21CC97109420961006400F8121072A0FEF332F561F9246" +
                "B156004D5547502F924D5500EE0808080808080800000000004FD6"
        );
        System.out.printf("Read %s of size %d.%n", fileName, fileSize);
        startMilliSeconds = System.currentTimeMillis();

        Runnable game = new Runnable() {
            @Override
            public void run() {
                long nextTick = 0;
                long sleepTime;
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

        game.run();
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
