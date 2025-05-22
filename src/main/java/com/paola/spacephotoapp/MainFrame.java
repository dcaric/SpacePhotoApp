package com.paola.spacephotoapp;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("NASA News Manager");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new JLabel("Welcome to NASA News Manager!", SwingConstants.CENTER));
    }
}