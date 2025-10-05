package org.example.view.panels;

import org.example.database.DatabaseManager;
import org.example.model.User;
import org.example.view.MainFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewHistory extends JPanel {
    private final User currentUser;
    private final MainFrame parentFrame;

    public ViewHistory(MainFrame parent, User user) {
        super(new BorderLayout());
        this.parentFrame = parent;
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(700, 400));

        // Create table model with column names
        String[] columnNames = {"Date & Time", "Duration", "Difficulty", "Level", "WPM", "Accuracy", "Errors"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        
        // Make table non-editable
        table.setDefaultEditor(Object.class, null);
        
        // Enable row selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add back button
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.showView("DASHBOARD");
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load data from database
        loadTestHistory(model);
    }

    /**
     * Checks if a table exists in the database
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private void loadTestHistory(DefaultTableModel model) {
        // Clear existing rows
        model.setRowCount(0);
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Check if table exists
            if (!tableExists(conn, "TEST_SESSION")) {
                showNoTestsMessage("The test history table doesn't exist yet.");
                return;
            }
            
            // SQL query to get test history
            String sql = "SELECT testDate, timeTaken, difficulty, level, wpm, accuracy, errors " +
                       "FROM TEST_SESSION WHERE userId = ? ORDER BY testDate DESC";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.getUserId());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean hasRecords = false;
                    
                    while (rs.next()) {
                        hasRecords = true;
                        try {
                            // Format the date and time
                            Timestamp timestamp = rs.getTimestamp("testDate");
                            String formattedDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
                            
                            // Convert milliseconds to minutes and seconds
                            long totalSeconds = rs.getLong("timeTaken") / 1000;
                            long minutes = totalSeconds / 60;
                            long seconds = totalSeconds % 60;
                            String formattedTime = String.format("%d:%02d", minutes, seconds);
                            
                            Object[] row = {
                                formattedDateTime,
                                formattedTime,
                                rs.getString("difficulty").toUpperCase(),
                                rs.getInt("level"),
                                rs.getInt("wpm"),
                                String.format("%.2f%%", rs.getDouble("accuracy")),
                                rs.getInt("errors")
                            };
                            model.addRow(row);
                        } catch (SQLException e) {
                            System.err.println("Error processing row: " + e.getMessage());
                        }
                    }
                    
                    if (!hasRecords) {
                        showNoTestsMessage("You haven't completed any typing tests yet.");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error in loadTestHistory: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error loading test history: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showNoTestsMessage(String message) {
        // Clear existing components
        removeAll();
        
        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create message label
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>"
                + "<h2>No Test History Found</h2>"
                + "<p>" + message + "</p>"
                + "<p>Complete a test to see your performance history here.</p>"
                + "</div></html>");
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Add message to main panel
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Add back button
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.showView("DASHBOARD");
            }
        });
        buttonPanel.add(backButton);
        
        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Update the UI
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
