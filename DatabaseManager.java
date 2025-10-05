package org.example.database;

import org.example.model.TestSession;
import org.example.model.User;
import org.example.util.PasswordHasher;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database operations for the Typing Performance Analyzer application.
 * Implements the singleton pattern to ensure only one instance exists.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:typing_analyzer.db";
    private static DatabaseManager instance;
    private Connection connection;
    
    static {
        try {
            LOGGER.info("Loading SQLite JDBC driver");
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load SQLite JDBC driver", e);
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    private DatabaseManager() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Gets the singleton instance of DatabaseManager.
     * @return The DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            instance.initializeDatabase();
        }
        return instance;
    }

    /**
     * Initializes the database by creating necessary tables and default data.
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            initializeDefaultPassages(conn);
            LOGGER.info("Database initialized successfully");
        } catch (SQLException e) {
            String error = "Failed to initialize database: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * Gets a connection to the database, creating a new one if necessary.
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.info("Connecting to database...");
            try {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement stmt = connection.createStatement()) {
                    // Enable foreign keys
                    stmt.execute("PRAGMA foreign_keys = ON");
                    // Enable WAL mode for better concurrency
                    stmt.execute("PRAGMA journal_mode = WAL");
                    // Set busy timeout
                    stmt.execute("PRAGMA busy_timeout = 5000");
                    // Enable case sensitive LIKE
                    stmt.execute("PRAGMA case_sensitive_like = ON");
                }
                LOGGER.info("Successfully connected to database");
            } catch (SQLException e) {
                String error = "Failed to connect to database: " + e.getMessage();
                LOGGER.log(Level.SEVERE, error, e);
                throw new SQLException(error, e);
            }
        }
        return connection;
    }

    /**
     * Creates the database tables if they don't exist.
     * @param conn The database connection to use
     * @throws SQLException if a database error occurs
     */
    /**
     * Checks if a column exists in a table.
     * @param conn The database connection
     * @param tableName The table name
     * @param columnName The column name
     * @return true if the column exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private void createTables(Connection conn) throws SQLException {
        LOGGER.info("Creating database tables if they don't exist...");
        try (Statement stmt = conn.createStatement()) {
            // Create USER table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS USER (
                    userId INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )""");
            LOGGER.info("Created USER table");
            
            // Create TEST_SESSION table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS TEST_SESSION (
                    sessionId INTEGER PRIMARY KEY AUTOINCREMENT,
                    userId INTEGER NOT NULL,
                    difficulty TEXT NOT NULL,
                    level INTEGER NOT NULL,
                    typedText TEXT,
                    timeTaken INTEGER,
                    wpm REAL,
                    accuracy REAL,
                    errors INTEGER,
                    testDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(userId) REFERENCES USER(userId) ON DELETE CASCADE
                )""");
            LOGGER.info("Created/Verified TEST_SESSION table");
            
            // Check if we need to add the level column to existing TEST_SESSION table
            if (!columnExists(conn, "TEST_SESSION", "level")) {
                LOGGER.info("Adding 'level' column to TEST_SESSION table");
                try (Statement alterStmt = conn.createStatement()) {
                    alterStmt.execute("ALTER TABLE TEST_SESSION ADD COLUMN level INTEGER DEFAULT 1");
                    LOGGER.info("Successfully added 'level' column to TEST_SESSION table");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to add 'level' column to TEST_SESSION table", e);
                    throw e;
                }
            }
            
            // Create PASSAGES table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS PASSAGES (
                    passageId INTEGER PRIMARY KEY AUTOINCREMENT,
                    difficulty TEXT NOT NULL,
                    level INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    UNIQUE(difficulty, level)
                )""");
            LOGGER.info("Created PASSAGES table");
            
            // Initialize default passages if they don't exist
            initializeDefaultPassages(conn);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating database tables", e);
            throw e;
        }
        LOGGER.info("Database tables created successfully");
    }

    // User Authentication Methods

    /**
     * Registers a new user with the given username and password.
     * @param username The username to register
     * @param password The plain text password
     * @return The newly created User object
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if username or password is invalid
     * @throws IllegalStateException if the username already exists
     */
    public User registerUser(String username, String password) throws SQLException {
        LOGGER.info("Attempting to register user: " + username);
        if (username == null || username.trim().isEmpty()) {
            String error = "Username cannot be empty";
            LOGGER.warning(error);
            throw new IllegalArgumentException(error);
        }
        if (password == null || password.isEmpty()) {
            String error = "Password cannot be empty";
            LOGGER.warning(error);
            throw new IllegalArgumentException(error);
        }

        if (userExists(username)) {
            String error = "Username already exists: " + username;
            LOGGER.warning(error);
            throw new IllegalStateException(error);
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        String sql = "INSERT INTO USER (username, password_hash, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, LocalDateTime.now().toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    return new User(userId, username, hashedPassword, LocalDateTime.now());
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            String error = "Failed to register user: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    /**
     * Checks if a user with the given username exists.
     * @param username The username to check
     * @return true if the user exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USER WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean exists = rs.next() && rs.getInt(1) > 0;
                LOGGER.info("User " + username + (exists ? " exists" : " does not exist") + " in the database");
                return exists;
            }
        } catch (SQLException e) {
            String error = "Failed to check if user exists: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    /**
     * Authenticates a user with the given username and password.
     * @param username The username
     * @param password The plain text password
     * @return The authenticated User object
     * @throws SecurityException if authentication fails
     * @throws SQLException if a database error occurs
     */
    public User loginUser(String username, String password) throws SQLException {
        LOGGER.info("Attempting login for user: " + username);
        String sql = "SELECT * FROM USER WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    LOGGER.info("Found user in database. Verifying password...");
                    boolean passwordMatch = PasswordHasher.verifyPassword(password, storedHash);
                    LOGGER.info("Password verification " + (passwordMatch ? "succeeded" : "failed"));
                    
                    if (passwordMatch) {
                        User user = new User(
                            rs.getInt("userId"),
                            rs.getString("username"),
                            storedHash,
                            LocalDateTime.parse(rs.getString("created_at"))
                        );
                        LOGGER.info("Login successful for user: " + username);
                        return user;
                    }
                } else {
                    LOGGER.warning("No user found with username: " + username);
                }
                throw new SecurityException("Invalid username or password");
            }
        } catch (SQLException e) {
            String error = "Login failed: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    // Test Session Methods

    /**
     * Saves a test session to the database.
     * @param session The test session to save
     * @throws SQLException if a database error occurs
     */
    public void saveTestSession(TestSession session) throws SQLException {
        String sql = """
            INSERT INTO TEST_SESSION 
            (userId, difficulty, level, typedText, timeTaken, wpm, accuracy, errors)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
            
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, session.getUserId());
            pstmt.setString(2, session.getDifficulty());
            pstmt.setInt(3, session.getLevel());
            pstmt.setString(4, session.getTypedText());
            pstmt.setLong(5, session.getTimeTaken());
            pstmt.setDouble(6, session.getWpm());
            pstmt.setDouble(7, session.getAccuracy());
            pstmt.setInt(8, session.getErrors());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            String error = "Failed to save test session: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    /**
     * Gets test sessions for a specific user, optionally filtered by difficulty.
     * @param userId The ID of the user
     * @param difficulty Optional difficulty to filter by (EASY, MEDIUM, HARD), or null for all
     * @return A list of test sessions
     * @throws SQLException if a database error occurs
     */
    public List<TestSession> getUserTestSessions(int userId, String difficulty) throws SQLException {
        String sql = "SELECT * FROM TEST_SESSION WHERE userId = ?";
        
        if (difficulty != null && !difficulty.isEmpty() && !difficulty.equalsIgnoreCase("All")) {
            sql += " AND UPPER(difficulty) = UPPER(?)";
        }
        
        sql += " ORDER BY testDate DESC";
        
        List<TestSession> sessions = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            if (difficulty != null && !difficulty.isEmpty() && !difficulty.equalsIgnoreCase("All")) {
                pstmt.setString(2, difficulty);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TestSession session = new TestSession(
                        rs.getInt("userId"),
                        rs.getString("difficulty"),
                        rs.getInt("level"),
                        rs.getString("typedText"),
                        rs.getLong("timeTaken"),
                        rs.getDouble("wpm"),
                        rs.getDouble("accuracy"),
                        rs.getInt("errors")
                    );
                    session.setSessionId(rs.getInt("sessionId"));
                    session.setTestDate(rs.getTimestamp("testDate").toLocalDateTime());
                    sessions.add(session);
                }
            }
        }
        
        return sessions;
    }
    
    /**
     * Gets all test sessions for a specific user.
     * @param userId The ID of the user
     * @return A list of test sessions
     * @throws SQLException if a database error occurs
     */
    public List<TestSession> getUserTestSessions(int userId) throws SQLException {
        return getUserTestSessions(userId, null);
    }

    /**
     * Gets all test sessions for a specific user.
     * @param userId The ID of the user
     * @return A list of test sessions
     * @throws SQLException if a database error occurs
     */
    public List<TestSession> getUserTestSessionsOld(int userId) throws SQLException {
        List<TestSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM TEST_SESSION WHERE userId = ? ORDER BY testDate DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TestSession session = new TestSession();
                    session.setSessionId(rs.getInt("sessionId"));
                    session.setUserId(rs.getInt("userId"));
                    session.setDifficulty(rs.getString("difficulty"));
                    session.setLevel(rs.getInt("level"));
                    session.setTypedText(rs.getString("typedText"));
                    session.setTimeTaken(rs.getInt("timeTaken"));
                    session.setWpm(rs.getDouble("wpm"));
                    session.setAccuracy(rs.getDouble("accuracy"));
                    session.setErrors(rs.getInt("errors"));
                    session.setTestDate(rs.getTimestamp("testDate").toLocalDateTime());
                    
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            String error = "Failed to get user test sessions: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
        return sessions;
    }

    // Passage Management Methods

    /**
     * Initializes default typing test passages in the database.
     * @param conn The database connection to use
     */
    private void initializeDefaultPassages(Connection conn) throws SQLException {
        String[][] defaultPassages = {
            {"EASY", "1", "The quick brown fox jumps over the lazy dog. This is a simple typing test for beginners."},
            {"EASY", "2", "Practice makes perfect. Keep typing to improve your speed and accuracy over time."},
            {"MEDIUM", "1", "The ability to type quickly and accurately is an essential skill in today's digital world."},
            {"MEDIUM", "2", "Regular practice can significantly improve your typing speed and reduce errors over time."},
            {"HARD", "1", "The quick brown fox jumps over the lazy dog. The five boxing wizards jump quickly. Pack my box with five dozen liquor jugs."},
            {"HARD", "2", "How vexingly quick daft zebras jump! The five boxing wizards jump quickly. Pack my box with five dozen liquor jugs."}
        };
        
        // Check if any passages exist
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PASSAGES")) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("No passages found. Adding default passages...");
                
                // Use a transaction to ensure all passages are added or none at all
                boolean autoCommit = conn.getAutoCommit();
                try {
                    conn.setAutoCommit(false);
                    
                    String insertSql = "INSERT OR IGNORE INTO PASSAGES (difficulty, level, content) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        for (String[] passage : defaultPassages) {
                            pstmt.setString(1, passage[0]);
                            pstmt.setInt(2, Integer.parseInt(passage[1]));
                            pstmt.setString(3, passage[2]);
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                    conn.commit();
                    LOGGER.info("Successfully added " + defaultPassages.length + " default passages");
                } catch (SQLException e) {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Failed to add default passages", e);
                    throw e;
                } finally {
                    conn.setAutoCommit(autoCommit);
                }
            } else {
                LOGGER.info("Passages already exist in the database");
            }
        }
    }
    
    /**
     * Gets a typing passage with the specified difficulty and level.
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD)
     * @param level The level within the difficulty (1, 2, etc.)
     * @return The passage content
     * @throws SQLException if a database error occurs or passage not found
     */
    public String getPassage(String difficulty, int level) throws SQLException {
        String sql = "SELECT content FROM PASSAGES WHERE difficulty = ? AND level = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, difficulty.toUpperCase());
            pstmt.setInt(2, level);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                } else {
                    throw new SQLException("No passage found for difficulty: " + difficulty + ", level: " + level);
                }
            }
        } catch (SQLException e) {
            String error = "Failed to get passage: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    /**
     * Saves or updates a typing passage.
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD)
     * @param level The level within the difficulty (1, 2, etc.)
     * @param content The passage content
     * @throws SQLException if a database error occurs
     */
    public void savePassage(String difficulty, int level, String content) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO PASSAGES (difficulty, level, content)
            VALUES (?, ?, ?)""";
            
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, difficulty.toUpperCase());
            pstmt.setInt(2, level);
            pstmt.setString(3, content);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            String error = "Failed to save passage: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            throw new SQLException(error, e);
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
