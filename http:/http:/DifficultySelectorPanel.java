
import org.example.model.DifficultyLevel;
import javax.swing.*;
import java.awt.*;

public class DifficultySelectorPanel extends JPanel {
    private JComboBox<DifficultyLevel> difficultyCombo;
    private JTextArea descriptionArea;
    
    public DifficultySelectorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Select Difficulty Level", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 20));
        JPanel comboPanel = new JPanel();
        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyCombo = new JComboBox<>(DifficultyLevel.values());
        comboPanel.add(difficultyLabel);
        comboPanel.add(difficultyCombo);
        
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(getBackground());
        updateDescription((DifficultyLevel) difficultyCombo.getSelectedItem());
        
        centerPanel.add(comboPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        difficultyCombo.addActionListener(e -> {
            DifficultyLevel selected = (DifficultyLevel) difficultyCombo.getSelectedItem();
            updateDescription(selected);
        });
    }
    
    private void updateDescription(DifficultyLevel level) {
        if (level != null) {
            descriptionArea.setText(String.format(
                "Level: %s\n" +
                "Time Limit: %d seconds\n" +
                "Word Length: %d-%d characters\n" +
                "Word Count: %d\n\n" +
                "Description: %s",
                level.getDisplayName(),
                level.getTimeLimit(),
                level.getMinWordLength(),
                level.getMaxWordLength(),
                level.getWordCount(),
                level.getDescription()
            ));
        }
    }
    
    public DifficultyLevel getSelectedDifficulty() {
        return (DifficultyLevel) difficultyCombo.getSelectedItem();
    }
}
