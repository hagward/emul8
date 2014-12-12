package hagward.chip8.emu;

import hagward.chip8.gui.Display;

import java.awt.Color;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Video {
    
    public static Color bgColor = Color.darkGray;
    public static Color fgColor = Color.white;

    private boolean[][] gfx;

    private final Display display;

    public Video(Display display) {
        this.display = display;
        gfx = new boolean[Display.DISPLAY_HEIGHT][Display.DISPLAY_WIDTH];
    }

    public void clear() {
        for (boolean[] row : gfx) {
            Arrays.fill(row, false);
        }
    }

    public boolean togglePixel(int x, int y) {
        return (gfx[y][x] = !gfx[y][x]);
    }

    public void flushVideo() {
        BufferedImage image = display.getImage();
        Graphics2D g = image.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, Display.DISPLAY_WIDTH * Display.DISPLAY_MODIFIER, Display.DISPLAY_HEIGHT
                * Display.DISPLAY_MODIFIER);

        for (int y = 0; y < Display.DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < Display.DISPLAY_WIDTH; x++) {
                if (gfx[y][x]) {
                    g.setColor(fgColor);
                    g.fillRect(x * Display.DISPLAY_MODIFIER, y * Display.DISPLAY_MODIFIER,
                            Display.DISPLAY_MODIFIER, Display.DISPLAY_MODIFIER);
                }
            }
        }

        g.dispose();
        display.repaint();
    }

}
