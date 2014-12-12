package hagward.chip8.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Queue;

public class StatusBar extends JPanel implements ActionListener {

    private final JLabel statusLabel;
    private final Timer timer;
    private final Queue<String> statusQueue;

    public StatusBar(int delay) {
        super(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel();
        add(statusLabel);

        timer = new Timer(delay, this);
        statusQueue = new ArrayDeque<>();
    }

    public void setStatus(String status) {
        timer.stop();
        statusLabel.setText(status);
    }

    public void setDelayedStatus(String status) {
        statusQueue.add(status);
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String status = statusQueue.poll();
        if (status != null) {
            statusLabel.setText(status);
        }
        if (statusQueue.isEmpty()) {
            timer.stop();
        }
    }
}
