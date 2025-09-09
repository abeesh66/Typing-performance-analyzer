package com.typinganalyzer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResultScreen extends JFrame {
    public ResultScreen(double wpm, double accuracy) {
        setTitle("Test Results");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String timeStamp = dtf.format(LocalDateTime.now());

        JLabel wpmLabel = new JLabel("Words per Minute: " + String.format("%.2f", wpm));
        JLabel accLabel = new JLabel("Accuracy: " + String.format("%.2f", accuracy) + "%");
        JLabel timeLabel = new JLabel("Test taken at: " + timeStamp);

        JButton backButton = new JButton("Back to Menu");

        panel.add(wpmLabel);
        panel.add(accLabel);
        panel.add(timeLabel);
        panel.add(backButton);

        add(panel);

        backButton.addActionListener(e -> {
            new MainMenu();
            dispose();
        });

        setVisible(true);
    }
}
