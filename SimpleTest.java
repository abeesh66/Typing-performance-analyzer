package org.example;

import javax.swing.*;
import java.awt.*;

public class SimpleTest {
    public static void main(String[] args) {
        try {
            // Set look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Create a simple frame
            JFrame frame = new JFrame("Simple Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            
            // Add a simple label
            JLabel label = new JLabel("If you can see this, the basic UI is working!");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(label, BorderLayout.CENTER);
            
            // Show the frame
            frame.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error in SimpleTest: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
