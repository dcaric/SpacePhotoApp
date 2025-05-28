package com.paola.spacephotoapp;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        //RssParser parser = new RssParser();
        //List<NewsRelease> newsList = parser.parse();

        NewsRepository repo = new NewsRepository();
        List<NewsRelease> newsList = repo.findAll();

        System.out.println("Loaded " + newsList.size() + " news items from database.");
        System.out.println("Parsed " + newsList.size() + " news items.");

        // Group by pubDate
        Map<String, List<NewsRelease>> grouped = newsList.stream()
                .collect(Collectors.groupingBy(NewsRelease::getPubDate));

        grouped.forEach((date, items) -> {
            System.out.println("Date: " + date + ", Count: " + items.size());
        });

        Set<String> uniqueDates = newsList.stream()
                .map(NewsRelease::getPubDate)
                .collect(Collectors.toSet());

        System.out.println("Unique publication dates: " + uniqueDates.size());

        //javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));

        // old way of starting MaiFrame without dialog before
        //SwingUtilities.invokeLater(() -> new MainFrame(newsList).setVisible(true));

        // Information dialog at startup
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    "Welcome to NASA News Viewer!\nUse the Next and Previous buttons to browse the news.\nClick OK to continue.",
                    "Welcome",
                    JOptionPane.INFORMATION_MESSAGE
            );
            new MainFrame(newsList).setVisible(true);
        });


    }
}