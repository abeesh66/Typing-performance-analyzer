package org.example.view.panels;

import org.example.view.MainFrame;

import javax.swing.*;
import java.awt.*;

public class LevelSelectionPanel extends JPanel {
    private final MainFrame parentFrame;
    private final String difficulty;
    
    public LevelSelectionPanel(MainFrame parent, String difficulty) {
        this.parentFrame = parent;
        this.difficulty = difficulty;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Select Level - " + difficulty, JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        // Create level buttons (1-3)
        for (int i = 1; i <= 3; i++) {
            JButton levelButton = new JButton("Level " + i);
            levelButton.setFont(new Font(levelButton.getFont().getName(), Font.PLAIN, 16));
            final int level = i;
            levelButton.addActionListener(e -> startTypingTest(level));
            buttonsPanel.add(levelButton);
        }
        
        // Back button
        JButton backButton = new JButton("Back to Difficulty Selection");
        backButton.addActionListener(e -> parentFrame.showView("DASHBOARD"));
        
        // Add components to panel
        add(titlePanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);
    }
    
    private void startTypingTest(int level) {
        parentFrame.startTypingTest(difficulty, level);
    }
}
