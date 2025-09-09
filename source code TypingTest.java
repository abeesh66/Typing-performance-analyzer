package com.typinganalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TypingTest extends JFrame {
    private JTextArea textToType;
    private JTextArea userInput;
    private long startTime;

    private String sampleText = "Typing is a fundamental skill that helps in improving speed and accuracy. "
            + "Practice daily to become better at typing.";

    public TypingTest() {
        setTitle("Typing Test");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textToType = new JTextArea(sampleText);
        textToType.setLineWrap(true);
        textToType.setWrapStyleWord(true);
        textToType.setEditable(false);
        textToType.setFont(new Font("Arial", Font.PLAIN, 16));

        userInput = new JTextArea();
        userInput.setLineWrap(true);
        userInput.setWrapStyleWord(true);
        userInput.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane scroll1 = new JScrollPane(textToType);
        JScrollPane scroll2 = new JScrollPane(userInput);

        JButton finishButton = new JButton("Finish Test");

        userInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                }
            }
        });

        finishButton.addActionListener(e -> calculateResults());

        setLayout(new BorderLayout());
        add(scroll1, BorderLayout.NORTH);
        add(scroll2, BorderLayout.CENTER);
        add(finishButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void calculateResults() {
        long endTime = System.currentTimeMillis();
        double timeTaken = (endTime - startTime) / 1000.0 / 60.0;

        String typedText = userInput.getText();

        int totalWords = typedText.isEmpty() ? 0 : typedText.split("\\s+").length;
        int correctChars = 0;
        int totalChars = Math.min(typedText.length(), sampleText.length());

        for (int i = 0; i < totalChars; i++) {
            if (typedText.charAt(i) == sampleText.charAt(i)) {
                correctChars++;
            }
        }

        double wpm = timeTaken > 0 ? totalWords / timeTaken : 0;
        double accuracy = (correctChars / (double) sampleText.length()) * 100;

        new ResultScreen(wpm, accuracy);
        dispose();
    }
}
