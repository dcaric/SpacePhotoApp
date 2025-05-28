// (View) handles the UI.

package com.paola.spacephotoapp.view;

import com.paola.spacephotoapp.controller.NewsController;
import com.paola.spacephotoapp.helping.NewsRepository;
import com.paola.spacephotoapp.helping.RssParser;
import com.paola.spacephotoapp.model.NewsRelease;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class MainFrame extends JFrame {
    private JLabel titleLabel;
    private JLabel imageLabel;
    private JTextArea descriptionArea;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel dateLabel;
    private JButton viewFullImageButton;

    private NewsController controller;

    public MainFrame(List<NewsRelease> newsList) {
        this.controller = new NewsController(newsList);

        setTitle("NASA News Viewer");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        updateDisplay();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(dateLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(600, 400));

        viewFullImageButton = new JButton("View Full Image");
        viewFullImageButton.addActionListener(e -> showFullImageDialog());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        centerPanel.add(viewFullImageButton, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        descriptionArea = new JTextArea();
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(350, 400));
        add(scrollPane, BorderLayout.EAST);

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        prevButton.addActionListener(e -> navigate(-1));
        nextButton.addActionListener(e -> navigate(1));

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        add(navPanel, BorderLayout.SOUTH);

        // refresh button
        JButton refreshButton = new JButton("Refresh Feed");
        refreshButton.addActionListener(e -> {
            RssParser parser = new RssParser();
            parser.parse(); // This should insert only new news

            // Reload updated list
            NewsRepository repo = new NewsRepository();
            List<NewsRelease> updatedList = repo.findAll();
            controller.setNewsList(updatedList);
            updateDisplay();
        });

        navPanel.add(refreshButton);

    }

    private void navigate(int offset) {
        if (offset > 0 && controller.hasNext()) {
            controller.nextNews();
        } else if (offset < 0 && controller.hasPrevious()) {
            controller.previousNews();
        }
        updateDisplay();
    }

    private void updateDisplay() {
        NewsRelease news = controller.getCurrentNews();
        if (news == null) return;

        titleLabel.setText(news.getTitle());
        descriptionArea.setText(news.getDescription());
        dateLabel.setText("Published: " + news.getPubDate());

        try {
            String path = news.getLocalImagePath();
            if (path != null) {
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    Image scaled = img.getScaledInstance(600, 400, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    return;
                }
            }
        } catch (Exception ignored) {}

        imageLabel.setIcon(null);
    }

    private void showFullImageDialog() {
        NewsRelease news = controller.getCurrentNews();
        String path = news.getLocalImagePath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "No image available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img != null) {
                ImageIcon fullIcon = new ImageIcon(img);
                JLabel fullImageLabel = new JLabel(fullIcon);
                JScrollPane scrollPane = new JScrollPane(fullImageLabel);
                scrollPane.setPreferredSize(new Dimension(1000, 700));

                JOptionPane.showMessageDialog(
                        this,
                        scrollPane,
                        "Full Image",
                        JOptionPane.PLAIN_MESSAGE
                );
            } else {
                throw new Exception("Image load failed.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load full image.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
