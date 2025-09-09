import org.example.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        try {
            // Initialize logger
            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "[%1$tF %1$tT] [%4$-7s] %5$s %n");
            
            LOGGER.info("Starting Typing Performance Analyzer...");
            
            // Set up uncaught exception handler
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                LOGGER.log(Level.SEVERE, "Uncaught exception in thread " + t.getName(), e);
                showErrorDialog("An unexpected error occurred: " + e.getMessage());
            });
            
            // Initialize database first
            initializeDatabase();
            
            // Initialize UI on the Event Dispatch Thread
            SwingUtilities.invokeAndWait(() -> {
                try {
                    initializeUI();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to initialize UI", e);
                    showErrorDialog("Failed to initialize user interface: " + e.getMessage());
                    System.exit(1);
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error during application startup", e);
            showErrorDialog("Fatal error during startup: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void initializeDatabase() {
        try {
            LOGGER.info("Initializing database...");
            // This will automatically initialize the database and create tables
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // Register shutdown hook for database cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Shutting down database...");
                    dbManager.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during database shutdown", e);
                }
            }));
            
            LOGGER.info("Database initialized successfully");
            
        } catch (Exception e) {
            String error = "Failed to initialize database: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            showErrorDialog(error);
            System.exit(1);
        }
    }
    
    private static void initializeUI() {
        try {
            LOGGER.info("Setting up look and feel...");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not set system look and feel", e);
            }
            
            LOGGER.info("Creating main window...");
            org.example.view.MainFrame frame = new org.example.view.MainFrame();
            frame.setVisible(true);
            frame.showView("LOGIN");
            
            LOGGER.info("Application started successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize UI", e);
            throw new RuntimeException("Failed to initialize user interface", e);
        }
    }
    
    private static void showErrorDialog(String message) {
        try {
            JOptionPane.showMessageDialog(
                null,
                message,
                "Application Error",
                JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception e) {
            // If we can't show a dialog, at least print to stderr
            System.err.println("ERROR: " + message);
        }
    }
}
