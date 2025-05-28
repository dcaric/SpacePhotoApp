// (Controller) manages state and navigation

package com.paola.spacephotoapp.controller;

import com.paola.spacephotoapp.model.NewsRelease;

import java.util.List;

public class NewsController {
    private List<NewsRelease> newsList;
    private int currentIndex;

    // ✅ Constructor that takes the list
    public NewsController(List<NewsRelease> newsList) {
        this.newsList = newsList;
        this.currentIndex = 0;
    }

    public NewsRelease getCurrentNews() {
        if (newsList == null || newsList.isEmpty()) return null;
        return newsList.get(currentIndex);
    }

    public boolean hasNext() {
        return newsList != null && currentIndex < newsList.size() - 1;
    }

    public boolean hasPrevious() {
        return newsList != null && currentIndex > 0;
    }

    public void nextNews() {
        if (hasNext()) currentIndex++;
    }

    public void previousNews() {
        if (hasPrevious()) currentIndex--;
    }
}
