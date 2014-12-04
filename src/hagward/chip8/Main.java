package hagward.chip8;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public class Main {
    
    private static String windowTitle = "EMUL8";
    
    private final Chip8 chip8;
    private final JFrame frame;
    
    public static void printDebug(String s) {
        System.out.println("dbg: " + s);
    }

    public Main() {
        final Display display = new Display(Display.DISPLAY_WIDTH * Display.DISPLAY_MODIFIER,
                                            Display.DISPLAY_HEIGHT * Display.DISPLAY_MODIFIER);
        final Keyboard keyboard = new Keyboard();
        chip8 = new Chip8(display, keyboard);
        frame = createAndShowGui(display);
        
        /*
        final File romFile = new File(romFileName);
        int fileSize = chip8.loadRom(romFile);
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
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);

                case KeyEvent.VK_P:
                    // TODO: Pause/resume the emulation.
                    if (chip8.isRunning()) {
                        chip8.stop();
                        frame.setTitle(frameTitle + " (paused)");
                    } else {
                        chip8.start();
                        frame.setTitle(frameTitle);
                    }
                    break;

                case KeyEvent.VK_BACK_SPACE:
                    chip8.reset();
                    chip8.loadRom(romFile);
                    break;

                default:
                    chip8.setKey(e.getKeyChar(), true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                chip8.setKey(e.getKeyChar(), false);
            }
        });

        frame.setVisible(true);
        chip8.start();
        */
    }
    
    public JFrame createAndShowGui(JPanel contentPane) {
        final JFrame frame = new JFrame(windowTitle);
        
        final JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        final JMenu fileMenu = new JMenu("File");
        final JMenu programMenu = new JMenu("Program");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        programMenu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(fileMenu);
        menuBar.add(programMenu);
        
        final JMenuItem openMenuItem = new JMenuItem("Open rom...", KeyEvent.VK_O);
        final JMenuItem quitMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        final JMenuItem startMenuItem = new JMenuItem("Start", KeyEvent.VK_S);
        final JMenuItem stopMenuItem = new JMenuItem("Stop", KeyEvent.VK_T);
        final JMenuItem pauseMenuItem = new JMenuItem("Pause", KeyEvent.VK_P);
        final JMenuItem resetMenuItem = new JMenuItem("Reset", KeyEvent.VK_R);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        startMenuItem.setAccelerator(KeyStroke.getKeyStroke("control R"));
        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke("control T"));
        pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke("P"));
        fileMenu.add(openMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(quitMenuItem);
        programMenu.add(startMenuItem);
        programMenu.add(stopMenuItem);
        programMenu.add(pauseMenuItem);
        programMenu.addSeparator();
        programMenu.add(resetMenuItem);
        
        // TODO: @refactor this into something more elegant.
        final ActionListener menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == openMenuItem) {
                    File romFile = getRomFromFileChooser();
                    if (romFile != null) {
                        chip8.reset();
                        chip8.loadRom(romFile);
                        chip8.start();
                    }
                } else if (e.getSource() == quitMenuItem) {
                    System.exit(0);
                } else if (e.getSource() == startMenuItem) {
                    chip8.start();
                } else if (e.getSource() == stopMenuItem) {
                    chip8.stop();
                    chip8.reset();
                } else if (e.getSource() == pauseMenuItem) {
                    if (chip8.isRunning()) {
                        chip8.stop();
                    } else {
                        chip8.start();
                    }
                } else if (e.getSource() == resetMenuItem) {
                    chip8.reset();
                }
            }
        };
        openMenuItem.addActionListener(menuListener);
        quitMenuItem.addActionListener(menuListener);
        startMenuItem.addActionListener(menuListener);
        stopMenuItem.addActionListener(menuListener);
        pauseMenuItem.addActionListener(menuListener);
        resetMenuItem.addActionListener(menuListener);
        
        frame.setContentPane(contentPane);
        frame.pack();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }
    
    private File getRomFromFileChooser() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    public static void main(String[] args) {
        new Main();
    }
    
}
