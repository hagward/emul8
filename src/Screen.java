import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Created by Anders on 2014-11-09.
 */
public class Screen extends JPanel {
    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;

    private Image image;

    public Screen() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    public void drawImage(Image image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
