package com.paola.spacephotoapp.helping;


import com.paola.spacephotoapp.dbservice.DatabaseService;
import com.paola.spacephotoapp.model.NewsRelease;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NewsRepository {
    private final DatabaseService db = DatabaseService.getInstance();

    public boolean existsByGuid(String guid) {
        return db.alreadyInDatabase(guid);
    }

    public void save(NewsRelease news) {
        db.insertNewsRelease(news);
    }

    public List<NewsRelease> findAll() {
        List<NewsRelease> newsList = new ArrayList<>();
        String sql = "SELECT title, description, link, guid, pubDate, imageUrl, localImagePath FROM NewsRelease ORDER BY pubDate DESC";

        try (Connection conn = DriverManager.getConnection(DatabaseService.CONNECTION_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                NewsRelease news = new NewsRelease();
                news.setTitle(rs.getString("title"));
                news.setDescription(rs.getString("description"));
                news.setLink(rs.getString("link"));
                news.setGuid(rs.getString("guid"));
                news.setPubDate(rs.getString("pubDate"));
                news.setImageUrl(rs.getString("imageUrl"));
                news.setLocalImagePath(rs.getString("localImagePath"));
                newsList.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

}
