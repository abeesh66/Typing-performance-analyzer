package com.typinganalyzer;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Typing Performance Analyzer");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton startButton = new JButton("Start Typing Test");
        JButton exitButton = new JButton("Exit");

        panel.add(startButton);
        panel.add(exitButton);

        add(panel);

        startButton.addActionListener(e -> {
            new TypingTest();
            dispose();
        });

        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }
}
