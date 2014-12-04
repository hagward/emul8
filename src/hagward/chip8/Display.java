package hagward.chip8;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Display extends JPanel {

    public static final int DISPLAY_WIDTH = 64;
    public static final int DISPLAY_HEIGHT = 32;
    public static final int DISPLAY_MODIFIER = 20;

    private final BufferedImage image;

    public Display(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        g.setColor(Video.bgColor);
        g.fillRect(0, 0, width, height);
        g.dispose();

        setPreferredSize(new Dimension(width, height));
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
