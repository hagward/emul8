package hagward.chip8.gui;

import hagward.chip8.emu.Chip8;
import hagward.chip8.emu.Keyboard;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;

public class Main {
    
    private static final String windowTitle = "EMUL8";
    
    private final Chip8 chip8;
    private final JFrame frame;

    private JLabel statusLabel;
    
    public static void printDebug(String s) {
        System.out.println("dbg: " + s);
    }

    public Main() {
        final Display display = new Display(Display.DISPLAY_WIDTH * Display.DISPLAY_MODIFIER,
                                            Display.DISPLAY_HEIGHT * Display.DISPLAY_MODIFIER);
        final Keyboard keyboard = new Keyboard();
        chip8 = new Chip8(display, keyboard);
        frame = createAndShowGui(display);
    }
    
    public JFrame createAndShowGui(JPanel contentPane) {
        final JFrame frame = new JFrame(windowTitle);
        frame.setLayout(new BorderLayout());
        
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
        final ActionListener menuListener = e -> {
            if (e.getSource() == openMenuItem) {
                File romFile = getRomFromFileChooser();
                if (romFile == null) {
                    return;
                }
                chip8.reset();
                int romSize = chip8.loadRom(romFile);
                chip8.start();
                statusLabel.setText(String.format("Loaded %s of %d bytes.", romFile.getName(), romSize));
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
                    statusLabel.setText("Paused.");
                } else {
                    chip8.start();
                    statusLabel.setText("Running.");
                }
            } else if (e.getSource() == resetMenuItem) {
                chip8.reset();
            }
        };
        openMenuItem.addActionListener(menuListener);
        quitMenuItem.addActionListener(menuListener);
        startMenuItem.addActionListener(menuListener);
        stopMenuItem.addActionListener(menuListener);
        pauseMenuItem.addActionListener(menuListener);
        resetMenuItem.addActionListener(menuListener);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Ready.");
        statusBar.add(statusLabel);

        frame.add(contentPane, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);
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
