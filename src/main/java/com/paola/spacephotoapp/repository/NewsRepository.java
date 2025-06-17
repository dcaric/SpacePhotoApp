package com.paola.spacephotoapp.repository;

import com.paola.spacephotoapp.dbservice.DatabaseService;
import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * This class is the concrete implementation of the `NewsRepositoryInterface`.
 * It acts as the bridge between the application's news-related logic and the actual database.
 * It uses the `DatabaseService` to perform the low-level database operations.
 */
public class NewsRepository implements NewsRepositoryInterface {

    // --- Database Service Instance ---
    // This line gets the single instance of the DatabaseService (due to its Singleton pattern).
    // The repository uses this service to interact with the database.
    private final DatabaseService db = DatabaseService.getInstance();

    /**
     * Implements the `insertNews` method from `NewsRepositoryInterface`.
     * It delegates the actual database insertion operation to the `DatabaseService`.
     * @param news The NewsRelease object to be inserted.
     */
    @Override
    public void insertNews(NewsRelease news) {
        db.insertNewsRelease(news); // Calls the DatabaseService to handle the insert.
    }

    /**
     * Implements the `existsByGuid` method from `NewsRepositoryInterface`.
     * It delegates the database check for an existing GUID to the `DatabaseService`.
     * @param guid The unique ID of the news release to check.
     * @return `true` if a news release with the given GUID exists, `false` otherwise.
     */
    @Override
    public boolean existsByGuid(String guid) {
        return db.alreadyInDatabase(guid); // Calls the DatabaseService to check existence.
    }

    /**
     * Implements the `getAllGuids` method from `NewsRepositoryInterface`.
     * This method directly performs a database query to fetch all GUIDs.
     * (Note: While `DatabaseService` has a similar method, this shows an alternative approach
     * where the repository directly handles a specific query if needed).
     *
     * @return A `Set` containing all unique GUID strings from the database.
     */
    @Override
    public Set<String> getAllGuids() {
        Set<String> guids = new HashSet<>(); // Initializes a set to store unique GUIDs.
        String sql = "SELECT guid FROM NewsRelease"; // SQL query to select all GUIDs.

        // Establishes a database connection and executes the query.
        // The try-with-resources ensures the connection and statement are closed automatically.
        try (Connection conn = DriverManager.getConnection(DatabaseService.CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { // Executes the query and gets the results.

            while (rs.next()) { // Iterates through each row in the result set.
                guids.add(rs.getString("guid")); // Adds the GUID from the current row to the set.
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Prints any database errors.
        }

        return guids; // Returns the set of GUIDs.
    }

    /**
     * Implements the `findAll` method from `NewsRepositoryInterface`.
     * This method retrieves all news releases from the database, orders them by publication date,
     * and maps the database records to `NewsRelease` objects.
     *
     * @return A `List` of all NewsRelease objects found in the database.
     */
    @Override
    public List<NewsRelease> findAll() {
        List<NewsRelease> newsList = new ArrayList<>(); // Initializes a list to store the retrieved news.
        // SQL query to select all relevant columns from the NewsRelease table, ordered by date.
        String sql = "SELECT title, description, link, guid, pubDate, imageUrl, category, localImagePath FROM NewsRelease ORDER BY pubDate DESC";

        // Establishes a database connection and executes the query.
        try (Connection conn = DriverManager.getConnection(DatabaseService.CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) { // Executes the query and gets the results.

            while (rs.next()) { // Iterates through each row returned by the query.
                NewsRelease news = new NewsRelease(); // Creates a new NewsRelease object for each row.
                // Populates the NewsRelease object with data from the current database row.
                news.setTitle(rs.getString("title"));
                news.setDescription(rs.getString("description"));
                news.setLink(rs.getString("link"));
                news.setGuid(rs.getString("guid"));
                news.setPubDate(rs.getString("pubDate"));
                news.setImageUrl(rs.getString("imageUrl"));
                // Converts the category string from the database back into a `NewsCategory` enum.
                news.setCategory(NewsCategory.valueOf(rs.getString("category")));
                news.setLocalImagePath(rs.getString("localImagePath"));
                newsList.add(news); // Adds the populated NewsRelease object to the list.
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Prints any database errors.
        }

        return newsList; // Returns the list of all news releases.
    }
}