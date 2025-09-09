src/main/java/org/example/model/TestSession.java
package org.example.model;

import java.time.LocalDateTime;

public class TestSession {
    private int sessionId;
    private int userId;
    private String difficulty;
    private String typedText;
    private int timeTaken; // in seconds
    private double wpm;    // words per minute
    private double accuracy;
    private int errors;
    private LocalDateTime testDate;
    private int level;

    public TestSession() {}

    public TestSession(int userId, String difficulty, String typedText, 
                      int timeTaken, double wpm, double accuracy, 
                      int errors, int level) {
        this.userId = userId;
        this.difficulty = difficulty;
        this.typedText = typedText;
        this.timeTaken = timeTaken;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.errors = errors;
        this.testDate = LocalDateTime.now();
        this.level = level;
    }

    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public String getTypedText() { return typedText; }
    public void setTypedText(String typedText) { this.typedText = typedText; }
    
    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }
    
    public double getWpm() { return wpm; }
    public void setWpm(double wpm) { this.wpm = wpm; }
    
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    
    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }
    
    public LocalDateTime getTestDate() { return testDate; }
    public void setTestDate(LocalDateTime testDate) { this.testDate = testDate; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}