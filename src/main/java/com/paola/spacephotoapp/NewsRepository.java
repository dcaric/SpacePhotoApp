package com.paola.spacephotoapp;

public class NewsRepository {
    private final DatabaseService db = DatabaseService.getInstance();

    public boolean existsByGuid(String guid) {
        return db.alreadyInDatabase(guid);
    }

    public void save(NewsRelease news) {
        db.insertNewsRelease(news);
    }
}
