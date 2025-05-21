package com.paola.spacephotoapp;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        RssParser parser = new RssParser();
        var photos = parser.parse();


        System.out.println("Parsed " + photos.size() + " photos:");




        // using optional
        /*
        for (SpacePhoto photo : photos) {
            System.out.print(photo.getTitle() + " -> ");
            Optional.ofNullable(photo.getImageUrl())
                    .ifPresentOrElse(
                            url -> System.out.println(url),
                            () -> System.out.println("No image")
                    );
        }
        */

        // functional programming instead of classic for loop
        photos.stream()
                .filter(photo -> photo.getImageUrl() != null)
                .map(photo -> photo.getTitle() + " -> " + photo.getImageUrl())
                .forEach(System.out::println);


        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
