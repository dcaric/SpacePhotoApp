package com.paola.spacephotoapp;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        RssParser parser = new RssParser();
        List<NewsRelease> newsList = parser.parse();

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

        javax.swing.SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}