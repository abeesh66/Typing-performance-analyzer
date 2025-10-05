package org.example.view.panels;

import org.example.view.MainFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DashboardPanel extends JPanel {
    private final MainFrame parent;
    public DashboardPanel(MainFrame parent) {
        this.parent = parent;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(0, 30));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        String welcomeMessage = "Welcome";
        
        // Check if user is logged in
        if (parent.getCurrentUser() != null) {
            welcomeMessage += ", " + parent.getCurrentUser().getUsername();
        }
        welcomeMessage += "!";
        
        JLabel welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font(welcomeLabel.getFont().getName(), Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        JButton startTestButton = createMenuButton("Start Typing Test");
        JButton viewHistoryButton = createMenuButton("View Typing History");
        JButton trackPerformanceButton = createMenuButton("Track Performance");
        JButton logoutButton = createMenuButton("Logout");
        
        // Add action listeners
        startTestButton.addActionListener(this::startTypingTest);
        viewHistoryButton.addActionListener(this::viewTypingHistory);
        trackPerformanceButton.addActionListener(this::trackPerformance);
        logoutButton.addActionListener(this::logout);
        
        buttonPanel.add(startTestButton);
        buttonPanel.add(viewHistoryButton);
        buttonPanel.add(trackPerformanceButton);
        buttonPanel.add(logoutButton);
        
        // Add components to main panel
        add(welcomePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }
    
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(300, 50));
        return button;
    }
    
    private void startTypingTest(ActionEvent e) {
        // Show difficulty selection dialog
        String[] options = {"Easy", "Medium", "Hard"};
        String selectedDifficulty = (String) JOptionPane.showInputDialog(
            this,
            "Select difficulty level:",
            "Difficulty Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (selectedDifficulty != null) {
            // Show level selection for the chosen difficulty
            parent.showLevelSelection(selectedDifficulty.toLowerCase());
        }
    }
    
    private void viewTypingHistory(ActionEvent e) {
        parent.showHistoryView();
    }
    
    private void trackPerformance(ActionEvent e) {
        parent.showPerformanceView();
    }
    
    private void logout(ActionEvent e) {
        parent.logoutUser();
    }
}
