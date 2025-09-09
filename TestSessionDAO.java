src/main/java/org/example/dao/TestSessionDAO.java
package org.example.dao;

import org.example.model.TestSession;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestSessionDAO {
    private final Connection connection;

    public TestSessionDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean saveTestSession(TestSession session) throws SQLException {
        String sql = "INSERT INTO test_sessions (user_id, difficulty, typed_text, time_taken, " +
                    "wpm, accuracy, errors, test_date, level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getUserId());
            stmt.setString(2, session.getDifficulty());
            stmt.setString(3, session.getTypedText());
            stmt.setInt(4, session.getTimeTaken());
            stmt.setDouble(5, session.getWpm());
            stmt.setDouble(6, session.getAccuracy());
            stmt.setInt(7, session.getErrors());
            stmt.setTimestamp(8, Timestamp.valueOf(session.getTestDate()));
            stmt.setInt(9, session.getLevel());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setSessionId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public List<TestSession> getSessionsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM test_sessions WHERE user_id = ? ORDER BY test_date DESC";
        List<TestSession> sessions = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToTestSession(rs));
                }
            }
        }
        return sessions;
    }

    public List<TestSession> getSessionsByUserAndDifficulty(int userId, String difficulty) throws SQLException {
        String sql = "SELECT * FROM test_sessions WHERE user_id = ? AND difficulty = ? ORDER BY test_date DESC";
        List<TestSession> sessions = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, difficulty);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToTestSession(rs));
                }
            }
        }
        return sessions;
    }

    private TestSession mapResultSetToTestSession(ResultSet rs) throws SQLException {
        TestSession session = new TestSession();
        session.setSessionId(rs.getInt("session_id"));
        session.setUserId(rs.getInt("user_id"));
        session.setDifficulty(rs.getString("difficulty"));
        session.setTypedText(rs.getString("typed_text"));
        session.setTimeTaken(rs.getInt("time_taken"));
        session.setWpm(rs.getDouble("wpm"));
        session.setAccuracy(rs.getDouble("accuracy"));
        session.setErrors(rs.getInt("errors"));
        session.setTestDate(rs.getTimestamp("test_date").toLocalDateTime());
        session.setLevel(rs.getInt("level"));
        return session;
    }
}