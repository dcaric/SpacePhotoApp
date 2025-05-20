package com.paola.spacephotoapp;

public class Main {
    public static void main(String[] args) {
        RssParser parser = new RssParser();
        var photos = parser.parse();

        System.out.println("Parsed " + photos.size() + " photos:");
        for (SpacePhoto photo : photos) {
            System.out.println(photo.getTitle() + " -> " + photo.getImageUrl());
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
