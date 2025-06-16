package com.paola.spacephotoapp.dbservice;

import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseService {

    // Instantiate one object - singleton
    private static final DatabaseService instance = new DatabaseService();

    public static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Photo;encrypt=true;trustServerCertificate=true;user=photo_user;password=photo123!";

    // Private constructor ensures no one else can create a DatabaseService
    // Single static instance is created eagerly at class load time.
    private DatabaseService() {}

    // Global access point via getInstance().
    public static DatabaseService getInstance() {
        return instance;
    }

    public static void insertNewsRelease(NewsRelease news) {

        // without procedure
        //String sql = "INSERT INTO NewsRelease (title, description, link, guid, pubDate, imageUrl, localImagePath) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // using procedure InsertNewsRelease
        String sql = "EXEC InsertNewsRelease ?, ?, ?, ?, ?, ?, ?, ?";
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getDescription());
            stmt.setString(3, news.getLink());
            stmt.setString(4, news.getGuid());
            stmt.setString(5, news.getPubDate());
            stmt.setString(6, news.getImageUrl());
            stmt.setString(7, news.getLocalImagePath());

            String category = (news.getCategory() != null) ? news.getCategory().name() : NewsCategory.UNKNOWN.name();
            stmt.setString(8, news.getCategory().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to insert news: " + news.getTitle());
            e.printStackTrace();
        }
    }

    public static boolean alreadyInDatabase(String guid) {
        String sql = "SELECT COUNT(*) FROM NewsRelease WHERE guid = ?";
        // Runtime (unchecked) exceptions
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, guid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Set<String> getAllGuids() {
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
}