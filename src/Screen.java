import javax.swing.*;
import java.awt.*;

/**
 * Created by Anders on 2014-11-09.
 */
public class Screen extends JPanel {
    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;

    public Screen() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    public void draw(int[] gfx) {
        Graphics g = getGraphics();
        g.setColor(Color.getHSBColor(0, 0, 14));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.getHSBColor(125, 61, 90));
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                for (int bit = 0; bit < 8; bit++) {
                    if ((128 >> bit) == 1) {
                        g.fillRect(x * 8 + bit, y * 8, 1, 8);
                    }
                }
            }
        }

        paintComponent(g);
    }
}
