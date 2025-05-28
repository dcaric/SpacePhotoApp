//  (Model) represents the data

package com.paola.spacephotoapp.model;

import com.paola.spacephotoapp.helping.MediaItem;
import com.paola.spacephotoapp.helping.NewsCategory;

import java.util.Objects;

public class NewsRelease extends MediaItem {
    private String link;
    private String guid;
    private String pubDate;
    private String imageUrl;
    private String localImagePath;
    private NewsCategory category = NewsCategory.UNKNOWN;

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }

    public String getPubDate() { return pubDate; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    public NewsCategory getCategory() { return category; }
    public void setCategory(NewsCategory category) { this.category = category; }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsRelease that = (NewsRelease) o;
        return Objects.equals(guid, that.guid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid);
    }
}