package com.paola.spacephotoapp.repository;

import com.paola.spacephotoapp.domain.model.NewsRelease;
import java.util.List;
import java.util.Set;

/**
 * This interface defines the contract for any class that wants to act as a "News Repository."
 * In software development, a repository acts as an intermediary between the application's business logic
 * and the data storage (like a database). It hides the complexity of how data is stored and retrieved.
 *
 * Any class that implements this interface must provide the specific functionality defined here,
 * ensuring a consistent way to manage `NewsRelease` objects.
 */
public interface NewsRepositoryInterface {
    /**
     * Inserts a new news release into the persistent storage (e.g., a database).
     * @param news The NewsRelease object to be inserted.
     */
    void insertNews(NewsRelease news);

    /**
     * Checks if a news release with a specific GUID (Global Unique Identifier) already exists in storage.
     * This helps prevent duplicate entries.
     * @param guid The unique ID of the news release to check.
     * @return `true` if a news release with the given GUID exists, `false` otherwise.
     */
    boolean existsByGuid(String guid);

    /**
     * Retrieves all unique GUIDs of news releases currently stored.
     * @return A `Set` containing all unique GUID strings.
     */
    Set<String> getAllGuids();

    /**
     * Retrieves all news releases from the persistent storage.
     * @return A `List` of all NewsRelease objects found.
     */
    List<NewsRelease> findAll();

    /**
     * A default method that prints the titles of all news releases in a given list.
     * Default methods in interfaces provide a basic implementation that can be used directly
     * by implementing classes, or overridden if a different behavior is needed.
     * @param newsList The list of NewsRelease objects whose titles are to be printed.
     */
    default void printAllTitles(List<NewsRelease> newsList) {
        newsList.forEach(news -> System.out.println(news.getTitle()));
    }
}