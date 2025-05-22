package com.paola.spacephotoapp;

import java.sql.*;

public class DatabaseService {
    private static final DatabaseService instance = new DatabaseService();

    private static final String CONNECTION_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Photo;encrypt=true;trustServerCertificate=true;user=photo_user;password=photo123!";

    private DatabaseService() {}

    public static DatabaseService getInstance() {
        return instance;
    }

    public static void insertNewsRelease(NewsRelease news) {
        String sql = "INSERT INTO NewsRelease (title, description, link, guid, pubDate, imageUrl, localImagePath) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getDescription());
            stmt.setString(3, news.getLink());
            stmt.setString(4, news.getGuid());
            stmt.setString(5, news.getPubDate());
            stmt.setString(6, news.getImageUrl());
            stmt.setString(7, news.getLocalImagePath());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to insert news: " + news.getTitle());
            e.printStackTrace();
        }
    }

    public static boolean alreadyInDatabase(String guid) {
        String sql = "SELECT COUNT(*) FROM NewsRelease WHERE guid = ?";
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
}