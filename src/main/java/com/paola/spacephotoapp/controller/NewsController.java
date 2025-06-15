// (Controller) manages state and navigation

package com.paola.spacephotoapp.controller;

import com.paola.spacephotoapp.domain.enums.NewsCategory;
import com.paola.spacephotoapp.domain.model.NewsRelease;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NewsController {
    private List<NewsRelease> allNews;
    private List<NewsRelease> filteredNews;
    private int currentIndex;

    // ✅ Constructor that takes the list
    public NewsController(List<NewsRelease> allNews) {
        this.allNews = new ArrayList<>(allNews);
        this.filteredNews = new ArrayList<>(allNews);
        this.currentIndex = 0;
    }

    public void filterByCategory(NewsCategory category) {
        if (category == null) {
            filteredNews = new ArrayList<>(allNews);
        } else {
            filteredNews = allNews.stream()
                    .filter(n -> n.getCategory() == category) // predicate usage
                    .collect(Collectors.toList()); // filter usage
        }
        currentIndex = 0;
    }

    /*
    public NewsRelease getCurrentNews() {
        if (filteredNews.isEmpty()) return null;
        return filteredNews.get(currentIndex);
    }
    */

    // with optional
    public Optional<NewsRelease> getOptionalCurrentNews() {
        return filteredNews.isEmpty() ? Optional.empty() : Optional.of(filteredNews.get(currentIndex));
    }

    public boolean hasNext() {
        return currentIndex < filteredNews.size() - 1;
    }

    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    public void nextNews() {
        if (hasNext()) currentIndex++;
    }

    public void previousNews() {
        if (hasPrevious()) currentIndex--;
    }

    public void setNewsList(List<NewsRelease> newsList) {
        this.allNews = new ArrayList<>(newsList);
        this.filteredNews = new ArrayList<>(newsList);
        currentIndex = 0;
    }
}
