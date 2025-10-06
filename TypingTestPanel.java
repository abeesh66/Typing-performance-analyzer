package org.example.view.panels;

import org.example.database.DatabaseManager;
import org.example.model.TestSession;
import org.example.model.User;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.example.view.MainFrame;
import org.example.util.TextSamples;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.sql.PreparedStatement;

public class TypingTestPanel extends JPanel {
    private final MainFrame parent;
    private final User currentUser;
    private final String difficulty;
    private final int level;
    private String sampleText;
    // Removed JTextArea as we're using KeyListener now
    private JLabel timerLabel;
    private JLabel wpmLabel;
    private JLabel accuracyLabel;
    private JButton startButton;
    private JButton doneButton;
    private Timer timer;
    private long startTime;
    private boolean isTestRunning = false;
    private List<String> wordsToType;
    private int currentWordIndex = 0;
    private int correctChars = 0;
    private int totalChars = 0;
    private int timeElapsed = 0; // in seconds
    private final StringBuilder typedText = new StringBuilder(); // Track typed characters

    private JTextArea sampleTextArea;
    private static final Logger LOGGER = Logger.getLogger(TypingTestPanel.class.getName()); // For displaying sample text only

    public TypingTestPanel(MainFrame parent, User user, String difficulty, int level) {
        this.parent = parent;
        this.currentUser = user;
        this.difficulty = difficulty;
        this.level = level;
        loadSampleText(); // Load text first
        initializeUI();   // Then initialize UI with the loaded text
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Initialize timer (don't start it yet)
        timer = new Timer(1000, e -> updateTimer());
        timer.setInitialDelay(0);

        // Timer and stats panel
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        timerLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
        timerLabel.setFont(new Font(timerLabel.getFont().getName(), Font.BOLD, 16));
        
        wpmLabel = new JLabel("WPM: 0", SwingConstants.CENTER);
        wpmLabel.setFont(new Font(wpmLabel.getFont().getName(), Font.BOLD, 16));
        
        accuracyLabel = new JLabel("Accuracy: 0%", SwingConstants.CENTER);
        accuracyLabel.setFont(new Font(accuracyLabel.getFont().getName(), Font.BOLD, 16));
        
        topPanel.add(timerLabel);
        topPanel.add(wpmLabel);
        topPanel.add(accuracyLabel);

        // Text display area
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createTitledBorder("Type the following text (" + difficulty + " - Level " + level + "):"));
        
        // Initialize the sample text area with the loaded text
        sampleTextArea = new JTextArea(sampleText);
        sampleTextArea.setWrapStyleWord(true);
        sampleTextArea.setLineWrap(true);
        sampleTextArea.setEditable(false);
        sampleTextArea.setBackground(new Color(240, 240, 240));
        sampleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        JScrollPane scrollPane = new JScrollPane(sampleTextArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add some padding around the text
        textPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Type the following text (Level 1):"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // User input area - Removed text area as we're using direct key logging
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Type the text above (press Enter to finish)"));
        
        // Add instruction label
        JLabel instructionLabel = new JLabel("Start typing to begin the test...");
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(instructionLabel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        startButton = new JButton("Start Test");
        startButton.addActionListener(e -> startTest());
        
        doneButton = new JButton("Done");
        doneButton.setEnabled(false);
        doneButton.addActionListener(e -> finishTest());
        
        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.addActionListener(e -> parent.showView("DASHBOARD"));
        
        JButton backToLevelsButton = new JButton("Back to Level Selection");
        backToLevelsButton.addActionListener(e -> parent.showLevelSelection(difficulty));
        
        // Create a sub-panel for the back buttons
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        backButtonPanel.add(backToLevelsButton);
        backButtonPanel.add(backToMenuButton);
        
        // Add components to button panel
        buttonPanel.setLayout(new BorderLayout());
        JPanel testButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        testButtonsPanel.add(startButton);
        testButtonsPanel.add(doneButton);
        
        buttonPanel.add(testButtonsPanel, BorderLayout.NORTH);
        buttonPanel.add(backButtonPanel, BorderLayout.SOUTH);

        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize timer
        timer = new Timer(1000, e -> updateTimer());
        
        // Add key listener to the panel
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!isTestRunning) return;
                
                char keyChar = e.getKeyChar();
                int keyCode = e.getKeyCode();
                
                // Log the key pressed
                System.out.println("Key typed: " + keyChar + " (" + (int)keyChar + ")");
                
                // Track cursor position using totalChars
                System.out.println("Cursor position: " + totalChars);
                
                // Update typing progress
                updateTypingProgress(keyChar);
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isTestRunning) {
                    finishTest();
                    e.consume();
                }
            }
        });
    }

    private void loadSampleText() {
        // Get sample text based on difficulty and level
        sampleText = TextSamples.getTextByLevel(difficulty, level);
        
        // Initialize wordsToType if null
        if (wordsToType == null) {
            wordsToType = new ArrayList<>();
        } else {
            wordsToType.clear();
        }
        
        // Split text into words for tracking
        wordsToType.addAll(List.of(sampleText.split("\\s+")));
        totalChars = sampleText.replaceAll("\\s+", "").length();
        
        // Update the text area if it's already initialized
        if (sampleTextArea != null) {
            sampleTextArea.setText(sampleText);
        }
    }

    private void startTest() {
        if (isTestRunning) return;
        
        isTestRunning = true;
        startTime = System.currentTimeMillis();
        timer.start();
        
        // Request focus for key events
        requestFocusInWindow();
        
        // Update UI
        startButton.setEnabled(false);
        doneButton.setEnabled(true);
        
        // Reset stats
        correctChars = 0;
        totalChars = 0;
        timeElapsed = 0;
        updateStats();
        
        System.out.println("Test started! Start typing...");
    }

    private void updateStats() {
        // Update WPM
        int wpm = calculateWPM();
        wpmLabel.setText("WPM: " + wpm);
        
        // Update accuracy
        updateAccuracy();
    }
    
    private void updateTimer() {
        timeElapsed++;
        timerLabel.setText("Time: " + timeElapsed + "s");
        
        // Update stats every second
        if (timeElapsed > 0) {
            updateStats();
        }
    }

    private void updateTypingProgress(char keyChar) {
        if (!isTestRunning) return;
        
        // For each key press, increment total characters
        totalChars++;
        
        // Add the typed character to our buffer
        typedText.append(keyChar);
        
        // Get the expected character at the current position
        int currentPos = totalChars - 1;
        if (currentPos < sampleText.length() && 
            sampleText.charAt(currentPos) == keyChar) {
            correctChars++;
        }
        
        // Update WPM and accuracy
        updateStats();
    }
    
    private void updateAccuracy() {
        if (totalChars > 0) {
            double accuracy = (double) correctChars / totalChars * 100;
            DecimalFormat df = new DecimalFormat("#.##");
            accuracyLabel.setText("Accuracy: " + df.format(accuracy) + "%");
        }
    }

    private int calculateWPM() {
        if (timeElapsed == 0) return 0;
        
        // Standard word is considered 5 characters
        int words = correctChars / 5;
        return (int) (words / (timeElapsed / 60.0));
    }

    private void finishTest() {
        isTestRunning = false;
        timer.stop();
        doneButton.setEnabled(false);
        startButton.setEnabled(true);
        
        int wpm = calculateWPM();
        double accuracy = totalChars > 0 ? (double) correctChars / totalChars * 100 : 0;
        int errors = totalChars - correctChars;
        
        // Save test result to database
        saveTestResult(wpm, accuracy);
        
        // Create a custom results panel
        JPanel resultsPanel = new JPanel(new GridBagLayout());
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add title
        JLabel titleLabel = new JLabel("Test Results");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        resultsPanel.add(titleLabel, gbc);
        
        // Add separator
        gbc.insets = new Insets(5, 0, 15, 0);
        resultsPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add result details
        addResultRow(resultsPanel, "Words per minute:", String.valueOf(wpm), gbc);
        addResultRow(resultsPanel, "Accuracy:", String.format("%.2f%%", accuracy), gbc);
        addResultRow(resultsPanel, "Time taken:", timeElapsed + " seconds", gbc);
        addResultRow(resultsPanel, "Difficulty:", difficulty, gbc);
        addResultRow(resultsPanel, "Errors:", String.valueOf(errors), gbc);
        
        // Add view history button
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton viewHistoryBtn = new JButton("View All Results");
        viewHistoryBtn.addActionListener(e -> showTestHistory());
        resultsPanel.add(viewHistoryBtn, gbc);
        
        // Show the results dialog
        JOptionPane.showMessageDialog(this, resultsPanel, "Test Results", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addResultRow(JPanel panel, String label, String value, GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        panel.add(new JLabel("<html><b>" + label + "</b></html>"), gbc);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        panel.add(new JLabel(value), gbc);
    }
    
    private void loadTestHistory(DefaultTableModel model) throws SQLException {
        String sql = "SELECT testDate, timeTaken, difficulty, level, wpm, accuracy, errors " +
                    "FROM TEST_SESSION WHERE userId = ? ORDER BY testDate DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            // Clear existing data
            model.setRowCount(0);
            
            // Add rows from result set
            while (rs.next()) {
                Object[] row = {
                    rs.getTimestamp("testDate"),
                    rs.getInt("wpm"),
                    String.format("%.2f%%", rs.getDouble("accuracy")),
                    rs.getInt("timeTaken"),
                    rs.getString("difficulty"),
                    rs.getInt("level"),
                    rs.getInt("errors")
                };
                model.addRow(row);
            }
        }
    }
    
    private void showTestHistory() {
        // Create a dialog to display test history
        JDialog historyDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Test History", true);
        historyDialog.setSize(800, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setLayout(new BorderLayout());
        
        // Create table model with column names
        String[] columnNames = {"Date & Time", "WPM", "Accuracy", "Time (s)", "Difficulty", "Level", "Errors"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Create table with model
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Date & Time
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // WPM
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Accuracy
        table.getColumnModel().getColumn(3).setPreferredWidth(70);  // Time
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Difficulty
        table.getColumnModel().getColumn(5).setPreferredWidth(50);  // Level
        table.getColumnModel().getColumn(6).setPreferredWidth(60);  // Errors
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load data in a background thread to prevent UI freeze
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadTestHistory(model);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        historyDialog,
                        "Error loading test history: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
        
        // Show the dialog
        historyDialog.setVisible(true);
    }
    
    private void saveTestResult(int wpm, double accuracy) {
        try {
            // Calculate errors (total characters - correct characters)
            int errors = totalChars - correctChars;
            
            // Create a new TestSession with the current level
            TestSession session = new TestSession(
                currentUser.getUserId(),
                difficulty,
                this.level,
                typedText.toString(),
                timeElapsed,
                wpm,
                accuracy,
                errors
            );
            
            LOGGER.info("Created test session: " + session);
            
            // Clear the typed text for the next test
            typedText.setLength(0);
            
            // Save to database
            DatabaseManager.getInstance().saveTestSession(session);
            
            System.out.println("Test result saved - WPM: " + wpm + ", Accuracy: " + accuracy + "%");
        } catch (SQLException e) {
            System.err.println("Error saving test result: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error saving test results: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}