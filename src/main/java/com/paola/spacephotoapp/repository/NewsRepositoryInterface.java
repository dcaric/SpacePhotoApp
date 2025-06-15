package com.paola.spacephotoapp.repository;

import com.paola.spacephotoapp.domain.model.NewsRelease;
import java.util.List;
import java.util.Set;

public interface NewsRepositoryInterface {
    void insertNews(NewsRelease news);
    boolean existsByGuid(String guid);
    Set<String> getAllGuids();
    List<NewsRelease> findAll();

    // default method - not used for now
    default void printAllTitles(List<NewsRelease> newsList) {
        newsList.forEach(news -> System.out.println(news.getTitle()));
    }

}
