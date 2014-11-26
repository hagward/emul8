package hagward.chip8;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class Main {
	
	public static void printDebug(String s) {
		System.out.println("dbg: " + s);
	}

    public Main(String romFileName) {
        final JFrame frame = new JFrame("EMUL8 - " + romFileName);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final Screen screen = new Screen(Chip8.SCREEN_WIDTH * Chip8.SCREEN_MODIFIER, Chip8.SCREEN_HEIGHT * Chip8.SCREEN_MODIFIER);
        frame.setContentPane(screen);

        frame.pack();
        frame.setLocationRelativeTo(null);

        final Chip8 chip8 = new Chip8();
        int fileSize = chip8.loadRom(new File(romFileName));
        if (fileSize == -1) {
        	System.exit(1);
        }
        System.out.printf("Read %s of size %d bytes.%n", romFileName, fileSize);

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                chip8.setKey(e.getKeyChar(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                chip8.setKey(e.getKeyChar(), false);
            }
        });
        
        frame.setVisible(true);

        Timer timer = new Timer(8, e -> {
            chip8.emulateCycle();
            if (chip8.isGfxUpdated()) {
                chip8.drawToImage(screen.getImage());
                screen.repaint();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
    	args = new String[] { "", "roms/TETRIS" };
    	if (args.length != 2) {
    		System.out.println("Usage: emul8 <rom>");
    	} else {
    		new Main(args[1]);
    	}
    }
}
