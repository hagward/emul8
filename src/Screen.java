import javax.swing.*;
import java.awt.*;

/**
 * Created by Anders on 2014-11-09.
 */
public class Screen extends JPanel {
    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;

    private int[] gfx;

    public Screen() {
        gfx = new int[64 * 32];
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    public void draw(int[] gfx) {
        this.gfx = gfx;
        repaint();
        System.out.println("draw() called");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.darkGray);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.green);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                for (int bit = 0; bit < 8; bit++) {
                    if ((128 >> bit) == 1) {
                        g.fillRect(x * 8 + bit, y * 8, 1, 8);
                    }
                }
            }
        }
    }
}
