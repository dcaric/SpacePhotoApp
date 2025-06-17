// (Controller) manages state and navigation
package com.paola.spacephotoapp.controller;

import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The NewsController acts as the central manager for displaying and navigating news articles.
 * It holds the full list of news, manages a filtered list (e.g., by category),
 * and keeps track of which news item is currently being viewed.
 * This class is part of the "Controller" component in a Model-View-Controller (MVC) architecture,
 * responsible for handling user input (like filtering or navigating) and updating the view.
 */
public class NewsController {
    // Stores the complete, unfiltered list of all news articles.
    private List<NewsRelease> allNews;
    // Stores the list of news articles currently being displayed (filtered by category).
    private List<NewsRelease> filteredNews;
    // Keeps track of the index of the news article currently in focus within the 'filteredNews' list.
    private int currentIndex;

    /**
     * Constructor for the NewsController.
     * It initializes the controller with a list of all available news articles.
     *
     * @param allNews The initial list of all news releases to be managed.
     */
    public NewsController(List<NewsRelease> allNews) {
        this.allNews = new ArrayList<>(allNews);      // Creates a new ArrayList to safely store a copy of all news.
        this.filteredNews = new ArrayList<>(allNews); // Initially, the filtered list is the same as all news.
        this.currentIndex = 0;                        // Starts by pointing to the first news item.
    }

    /**
     * Filters the list of news articles by a specific category.
     * If a null category is provided, it resets the filter to show all news.
     * After filtering, the current view index is reset to the first item of the new filtered list.
     *
     * @param category The NewsCategory to filter by (e.g., MOON, MARS), or null to show all news.
     */
    public void filterByCategory(NewsCategory category) {
        System.out.println("filteredNews (before filter): " + filteredNews);

        if (category == null) {
            // If category is null, show all news.
            filteredNews = new ArrayList<>(allNews);
        } else {
            // Uses Java Streams to efficiently filter the 'allNews' list.
            filteredNews = allNews.stream()
                    .filter(n -> n.getCategory() == category)
                    .collect(Collectors.toList());

            System.out.println("filteredNews (after filter): " + filteredNews);
        }
        // Resets the current index to the beginning of the new filtered list.
        currentIndex = 0;
    }

    /**
     * Provides a copy of the complete list of all news articles managed by the controller.
     *
     * @return A new ArrayList containing all news releases.
     */
    public List<NewsRelease> getNewsList() {
        return new ArrayList<>(allNews); // Returns a copy to prevent external modification of the internal list.
    }

    /*
    // This is an older version of getting the current news, which might return null directly.
    // public NewsRelease getCurrentNews() { ... }
    */

    /**
     * Provides the currently selected news article as an Optional.
     * Using Optional helps to explicitly handle cases where there might be no news
     * (e.g., after a filter results in an empty list) without returning a null value directly.
     *
     * @return An Optional containing the current NewsRelease, or an empty Optional if the filtered list is empty.
     */
    public Optional<NewsRelease> getOptionalCurrentNews() {
        return filteredNews.isEmpty() ? Optional.empty() : Optional.of(filteredNews.get(currentIndex));
    }

    /**
     * Checks if there is a next news article available in the filtered list.
     *
     * @return `true` if moving to the next item is possible, `false` otherwise.
     */
    public boolean hasNext() {
        return currentIndex < filteredNews.size() - 1;
    }

    /**
     * Checks if there is a previous news article available in the filtered list.
     *
     * @return `true` if moving to the previous item is possible, `false` otherwise.
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Moves the current news pointer to the next news article in the filtered list.
     * This method does nothing if there is no next item (checked by `hasNext()`).
     */
    public void nextNews() {
        if (hasNext()) currentIndex++;
    }

    /**
     * Moves the current news pointer to the previous news article in the filtered list.
     * This method does nothing if there is no previous item (checked by `hasPrevious()`).
     */
    public void previousNews() {
        if (hasPrevious()) currentIndex--;
    }

    /**
     * Replaces the entire list of news articles managed by the controller.
     * This is useful for loading new data or refreshing the content.
     * After setting a new list, the filter is reset and the view returns to the first item.
     *
     * @param newsList The new list of NewsRelease objects to set.
     */
    public void setNewsList(List<NewsRelease> newsList) {
        this.allNews = new ArrayList<>(newsList);
        this.filteredNews = new ArrayList<>(newsList);
        currentIndex = 0;
    }
}