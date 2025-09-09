src/main/java/org/example/view/panels/PerformancePanel.java
package org.example.view.panels;

import org.example.model.TestSession;
import org.example.service.StatisticsService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PerformancePanel extends JPanel {
    private final List<TestSession> userSessions;
    private final JLabel avgWpmLabel;
    private final JLabel bestWpmLabel;
    private final JLabel avgAccuracyLabel;
    private final JLabel totalTestsLabel;
    private final JPanel chartPanel;

    public PerformancePanel(List<TestSession> userSessions) {
        this.userSessions = userSessions;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Stats Panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);

        // Chart Panel
        chartPanel = new JPanel(new BorderLayout());
        updateCharts();
        add(new JScrollPane(chartPanel), BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Performance Summary"));

        avgWpmLabel = createStatLabel("Average WPM: 0.0");
        bestWpmLabel = createStatLabel("Best WPM: 0.0");
        avgAccuracyLabel = createStatLabel("Average Accuracy: 0.0%");
        totalTestsLabel = createStatLabel("Total Tests: 0");

        panel.add(avgWpmLabel);
        panel.add(bestWpmLabel);
        panel.add(avgAccuracyLabel);
        panel.add(totalTestsLabel);

        updateStats();
        return panel;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }

    private void updateStats() {
        if (userSessions == null || userSessions.isEmpty()) return;

        double avgWpm = StatisticsService.calculateAverageWPM(userSessions);
        double bestWpm = StatisticsService.calculateBestWPM(userSessions);
        double avgAccuracy = StatisticsService.calculateAverageAccuracy(userSessions);
        int totalTests = StatisticsService.calculateTotalTests(userSessions);

        avgWpmLabel.setText(String.format("Average WPM: %.1f", avgWpm));
        bestWpmLabel.setText(String.format("Best WPM: %.1f", bestWpm));
        avgAccuracyLabel.setText(String.format("Average Accuracy: %.1f%%", avgAccuracy));
        totalTestsLabel.setText(String.format("Total Tests: %d", totalTests));
    }

    private void updateCharts() {
        chartPanel.removeAll();
        
        // Create a panel with two charts side by side
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // WPM by Difficulty Chart
        JFreeChart difficultyChart = createChart(
            "Average WPM by Difficulty", 
            StatisticsService.getAverageWPMByDifficulty(userSessions)
        );
        chartsPanel.add(new ChartPanel(difficultyChart));
        
        // WPM by Level Chart
        JFreeChart levelChart = createChart(
            "Average WPM by Level", 
            StatisticsService.getAverageWPMByLevel(userSessions).entrySet()
                .stream()
                .collect(Collectors.toMap(
                    e -> "Level " + e.getKey(), 
                    Map.Entry::getValue
                ))
        );
        chartsPanel.add(new ChartPanel(levelChart));
        
        chartPanel.add(chartsPanel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private JFreeChart createChart(String title, Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((key, value) -> 
            dataset.addValue(value, "WPM", key)
        );

        return ChartFactory.createBarChart(
            title,
            "Category",
            "Words Per Minute (WPM)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
    }
}