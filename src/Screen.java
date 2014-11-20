import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by Anders on 2014-11-09.
 */
public class Screen extends JPanel {
    private static final int WIDTH = 512;
    private static final int HEIGHT = 256;

    private BufferedImage image;

    public Screen() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
