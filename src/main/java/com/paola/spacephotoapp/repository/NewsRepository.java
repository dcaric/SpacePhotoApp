package com.paola.spacephotoapp.repository;


import com.paola.spacephotoapp.dbservice.DatabaseService;
import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.sql.*;
import java.util.*;
import java.util.List;

public class NewsRepository implements NewsRepositoryInterface {

    // singleton DatabaseService usage, get object created at class load
    // via getInstance()
    private final DatabaseService db = DatabaseService.getInstance();

    @Override
    public void insertNews(NewsRelease news) {
        db.insertNewsRelease(news);
    }

    @Override
    public boolean existsByGuid(String guid) {
        return db.alreadyInDatabase(guid);
    }

    @Override
    public Set<String> getAllGuids() {
        Set<String> guids = new HashSet<>();
        String sql = "SELECT guid FROM NewsRelease";

        try (Connection conn = DriverManager.getConnection(DatabaseService.CONNECTION_URL);
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

    @Override
    public List<NewsRelease> findAll() {
        List<NewsRelease> newsList = new ArrayList<>();
        String sql = "SELECT title, description, link, guid, pubDate, imageUrl, category, localImagePath FROM NewsRelease ORDER BY pubDate DESC";

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
                news.setCategory(NewsCategory.valueOf(rs.getString("category")));
                news.setLocalImagePath(rs.getString("localImagePath"));
                newsList.add(news);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newsList;
    }
}
