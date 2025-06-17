package com.paola.spacephotoapp.dbservice;

import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;


/**
 * This class handles all interactions with the database for the application.
 * It's designed as a **Singleton**, which means only one instance of this class
 * can exist in the entire application. This ensures that all database operations
 * go through a single, controlled point.
 */
public class DatabaseService {

    // --- Singleton Instance ---
    // This creates the single instance of the DatabaseService when the class is loaded.
    private static final DatabaseService instance = new DatabaseService();

    // --- Database Connection Details ---
    // This string contains all the necessary information to connect to the SQL Server database:
    // server address (localhost:1433), database name (Photo), and credentials (user/password).
    // 'encrypt=true;trustServerCertificate=true' are for secure connection.
    public static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Photo;encrypt=true;trustServerCertificate=true;user=photo_user;password=photo123!";

    // --- Private Constructor for Singleton Pattern ---
    // By making the constructor private, no other part of the code can create new
    // DatabaseService objects directly, enforcing the singleton pattern.
    private DatabaseService() {}

    // --- Public Access Method for Singleton ---
    // This is the only way to get the instance of the DatabaseService.
    // Any part of the application that needs to interact with the database will call this method.
    public static DatabaseService getInstance() {
        return instance;
    }


    /**
     * Inserts a new news release record into the database.
     * It uses a **stored procedure** named 'InsertNewsRelease' on the database server
     * for organized and potentially more secure data insertion.
     *
     * @param news The NewsRelease object containing all the data to be inserted.
     */
    public static void insertNewsRelease(NewsRelease news) {
        // The SQL command to execute the stored procedure with placeholders (?) for values.
        String sql = "EXEC InsertNewsRelease ?, ?, ?, ?, ?, ?, ?, ?";
        // The 'try-with-resources' statement ensures that the Connection and PreparedStatement
        // are automatically closed when the block finishes, even if errors occur.
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL); // Establishes a connection to the database.
             PreparedStatement stmt = conn.prepareStatement(sql)) { // Prepares the SQL statement, preventing SQL injection.

            // Sets the values for each placeholder in the SQL statement, matching the order
            // expected by the 'InsertNewsRelease' stored procedure.
            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getDescription());
            stmt.setString(3, news.getLink());
            stmt.setString(4, news.getGuid());
            stmt.setString(5, news.getPubDate());
            stmt.setString(6, news.getImageUrl());
            stmt.setString(7, news.getLocalImagePath());

            // Converts the NewsCategory enum to its string name for database storage.
            // If category is null, it defaults to 'UNKNOWN'.
            String category = (news.getCategory() != null) ? news.getCategory().name() : NewsCategory.UNKNOWN.name();
            // Binds the category string to the 8th parameter.
            stmt.setString(8, news.getCategory().toString());

            stmt.executeUpdate(); // Executes the SQL command to insert the data.

        } catch (SQLException e) {
            // Catches any database-related errors during the insertion process.
            System.err.println("Failed to insert news: " + news.getTitle());
            e.printStackTrace();
        }
    }


    /**
     * Checks if a news release with a specific Global Unique Identifier (GUID) already exists in the database.
     * This helps prevent inserting duplicate news articles.
     *
     * @param guid The GUID of the news release to check.
     * @return `true` if a record with the given GUID exists, `false` otherwise.
     */
    public static boolean alreadyInDatabase(String guid) {
        // SQL query to count records where the GUID matches.
        String sql = "SELECT COUNT(*) FROM NewsRelease WHERE guid = ?";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sets the GUID for the query.
            stmt.setString(1, guid);
            // Executes the query and gets the result.
            ResultSet rs = stmt.executeQuery();
            // Checks if there's a result (rs.next()) and if the count is greater than 0.
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            // Catches any database errors during the check.
            e.printStackTrace();
        }
        return false; // Returns false if any error occurs or no record is found.
    }

    public static Set<String> getAllGuids1() {
        Set<String> guids = new HashSet<>();
        String sql = "SELECT guid FROM NewsRelease";
        // Runtime (unchecked) exceptions
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                guids.add(rs.getString("guid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guids;
    }

    /**
     * Retrieves all Global Unique Identifiers (GUIDs) from the NewsRelease table in the database.
     * This is useful for efficiently checking for duplicates or managing existing records
     * without fetching all news details.
     *
     * @return A `Set` of all GUID strings found in the database. A `Set` automatically
     * handles uniqueness, so there won't be duplicate GUIDs in the returned collection.
     */
    public static Set<String> getAllGuids() {
        // Creates a new HashSet to store unique GUIDs.
        Set<String> guids = new HashSet<>();
        String sql = "SELECT guid FROM NewsRelease"; // SQL query to select all GUIDs.
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);

             // Executes the query and gets the results.
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Adds the GUID from the current row to the set.
                guids.add(rs.getString("guid"));
            }
        } catch (SQLException e) {
            // Catches any database errors during the retrieval process.
            e.printStackTrace();
        }
        return guids;
    }
}