package org.example.view;

import org.example.model.User;
import org.example.view.panels.DashboardPanel;
import org.example.view.panels.DifficultyPanel;
import org.example.view.panels.LoginPanel;
import org.example.view.panels.RegisterPanel;
import org.example.view.panels.TypingTestPanel;
import org.example.view.panels.ViewHistory;
import org.example.view.panels.LevelSelectionPanel;
import org.example.view.panels.PerformancePanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final String TITLE = "Typing Performance Analyzer";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private User currentUser;
    
    public MainFrame() {
        try {
            System.out.println("1. Starting MainFrame initialization...");
            setTitle(TITLE);
            setSize(WIDTH, HEIGHT);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(true);
            System.out.println("2. Basic frame properties set");
            
            // Initialize card layout for view switching
            System.out.println("3. Initializing card layout...");
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            
            // Initialize and add all panels
            System.out.println("4. Initializing panels...");
            try {
                initializePanels();
                System.out.println("5. Panels initialized successfully");
            } catch (Exception e) {
                System.err.println("Error in initializePanels(): " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize panels: " + e.getMessage(), e);
            }
            
            System.out.println("6. Adding card panel to frame...");
            add(cardPanel);
            
            // Show login panel by default
            System.out.println("7. Showing login view...");
            try {
                showView("LOGIN");
                System.out.println("8. Successfully showed login view");
            } catch (Exception e) {
                System.err.println("Error showing login view: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to show login view: " + e.getMessage(), e);
            }
            
            System.out.println("9. Setting window visible...");
            setVisible(true);
            toFront();
            requestFocus();
            System.out.println("10. MainFrame initialization complete!");
            
        } catch (Exception e) {
            String errorMsg = "Critical error in MainFrame initialization: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            
            // Show detailed error in console
            System.err.println("\n=== STACK TRACE ===");
            e.printStackTrace();
            
            // Show error dialog with more details
            String message = "<html><body width='300px'>" +
                          "<h3>Failed to initialize application</h3>" +
                          "<p>Error: " + e.getClass().getSimpleName() + "</p>" +
                          "<p>Message: " + e.getMessage() + "</p>" +
                          "<p>Check console for more details.</p>" +
                          "</body></html>";
                          
            JOptionPane.showMessageDialog(
                null, 
                message,
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE
            );
            
            // Exit the application as we can't recover from this
            System.exit(1);
        }
    }
    
    public void addView(JPanel panel, String name) {
        cardPanel.add(panel, name);
    }
    
    private void initializePanels() {
        try {
            System.out.println("4.1 Creating LoginPanel...");
            LoginPanel loginPanel = new LoginPanel(this);
            addView(loginPanel, "LOGIN");
            System.out.println("4.2 LoginPanel created successfully");
            
            System.out.println("4.3 Creating RegisterPanel...");
            RegisterPanel registerPanel = new RegisterPanel(this);
            addView(registerPanel, "REGISTER");
            System.out.println("4.4 RegisterPanel created successfully");
            
            System.out.println("4.5 Creating DashboardPanel...");
            DashboardPanel dashboardPanel = new DashboardPanel(this);
            addView(dashboardPanel, "DASHBOARD");
            System.out.println("4.6 DashboardPanel created successfully");
            
            System.out.println("4.7 Creating DifficultyPanel...");
            DifficultyPanel difficultyPanel = new DifficultyPanel(this, null);
            addView(difficultyPanel, "DIFFICULTY");
            System.out.println("4.8 DifficultyPanel created successfully");
            
            System.out.println("4.9 Creating TypingTestPanel...");
            TypingTestPanel typingTestPanel = new TypingTestPanel(this, null, "medium", 1);
            addView(typingTestPanel, "TYPING_TEST");
            System.out.println("4.10 TypingTestPanel created successfully");
            
        } catch (Exception e) {
            System.err.println("Error in initializePanels: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize panels: " + e.getMessage(), e);
        }
    }
    
    public void showView(String name) {
        cardLayout.show(cardPanel, name);
    }
    
    public void logoutUser() {
        this.currentUser = null;
        showView("LOGIN");
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void showDifficultySelection(User user) {
        setCurrentUser(user);
        // Create a new difficulty panel each time to ensure fresh state
        DifficultyPanel difficultyPanel = new DifficultyPanel(this, currentUser);
        addView(difficultyPanel, "DIFFICULTY");
        showView("DIFFICULTY");
    }
    
    public void showLevelSelection(String difficulty) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please log in to take a typing test.",
                "Authentication Required",
                JOptionPane.WARNING_MESSAGE);
            showView("LOGIN");
            return;
        }
        
        // Create a new level selection panel with the selected difficulty
        LevelSelectionPanel levelSelectionPanel = new LevelSelectionPanel(this, difficulty);
        
        // Remove any existing LEVEL_SELECTION panel to avoid memory leaks
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof LevelSelectionPanel) {
                cardPanel.remove(comp);
            }
        }
        
        // Add the new level selection panel and show it
        addView(levelSelectionPanel, "LEVEL_SELECTION");
        showView("LEVEL_SELECTION");
    }
    
    public void startTypingTest(String difficulty, int level) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please log in to take a typing test.",
                "Authentication Required",
                JOptionPane.WARNING_MESSAGE);
            showView("LOGIN");
            return;
        }
        
        // Create a new typing test panel with the selected difficulty and level
        TypingTestPanel typingTestPanel = new TypingTestPanel(this, currentUser, difficulty, level);
        
        // Remove any existing TYPING_TEST panel to avoid memory leaks
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof TypingTestPanel) {
                cardPanel.remove(comp);
            }
        }
        
        addView(typingTestPanel, "TYPING_TEST");
        showView("TYPING_TEST");
    }
    
    public void showHistoryView() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please log in to view your history.",
                "Authentication Required",
                JOptionPane.WARNING_MESSAGE);
            showView("LOGIN");
            return;
        }
        
        try {
            // Create a new history panel with the current user
            ViewHistory viewHistory = new ViewHistory(this, currentUser);
            
            // Remove any existing VIEW_HISTORY panel to avoid memory leaks
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof ViewHistory) {
                    cardPanel.remove(comp);
                }
            }
            
            // Add the new history panel and show it
            addView(viewHistory, "VIEW_HISTORY");
            showView("VIEW_HISTORY");
        } catch (Exception e) {
            System.err.println("Error showing history view: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading history: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Shows the performance tracking view with user's typing statistics.
     */
    public void showPerformanceView() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please log in to view your performance.",
                "Authentication Required",
                JOptionPane.WARNING_MESSAGE);
            showView("LOGIN");
            return;
        }
        
        try {
            // Create a new performance panel with the current user
            PerformancePanel performancePanel = new PerformancePanel(this, currentUser);
            
            // Remove any existing PERFORMANCE panel to avoid memory leaks
            for (Component comp : cardPanel.getComponents()) {
                if (comp instanceof PerformancePanel) {
                    cardPanel.remove(comp);
                }
            }
            
            // Add the new performance panel and show it
            addView(performancePanel, "PERFORMANCE");
            showView("PERFORMANCE");
        } catch (Exception e) {
            System.err.println("Error showing performance view: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading performance data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and show the main frame
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                
                // Show login view
                frame.showView("LOGIN");
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error initializing application: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
