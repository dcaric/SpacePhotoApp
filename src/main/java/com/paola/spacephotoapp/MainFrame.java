package com.paola.spacephotoapp;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Movie Manager");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Simple label for testing
        JLabel label = new JLabel("Hello from MainFrame!", SwingConstants.CENTER);
        add(label);
    }
}
