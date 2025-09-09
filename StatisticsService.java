src/main/java/org/example/service/StatisticsService.java
package org.example.service;

import org.example.model.TestSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {
    public static double calculateAverageWPM(List<TestSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return 0.0;
        return sessions.stream()
                .mapToDouble(TestSession::getWpm)
                .average()
                .orElse(0.0);
    }

    public static double calculateBestWPM(List<TestSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return 0.0;
        return sessions.stream()
                .mapToDouble(TestSession::getWpm)
                .max()
                .orElse(0.0);
    }

    public static double calculateAverageAccuracy(List<TestSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return 0.0;
        return sessions.stream()
                .mapToDouble(TestSession::getAccuracy)
                .average()
                .orElse(0.0);
    }

    public static int calculateTotalTests(List<TestSession> sessions) {
        return sessions != null ? sessions.size() : 0;
    }

    public static int calculateTotalTypedWords(List<TestSession> sessions) {
        if (sessions == null) return 0;
        return sessions.stream()
                .mapToInt(session -> session.getTypedText().split("\\s+").length)
                .sum();
    }

    public static Map<String, Double> getAverageWPMByDifficulty(List<TestSession> sessions) {
        if (sessions == null) return Map.of();
        return sessions.stream()
                .collect(Collectors.groupingBy(
                    TestSession::getDifficulty,
                    Collectors.averagingDouble(TestSession::getWpm)
                ));
    }

    public static Map<Integer, Double> getAverageWPMByLevel(List<TestSession> sessions) {
        if (sessions == null) return Map.of();
        return sessions.stream()
                .collect(Collectors.groupingBy(
                    TestSession::getLevel,
                    Collectors.averagingDouble(TestSession::getWpm)
                ));
    }
}