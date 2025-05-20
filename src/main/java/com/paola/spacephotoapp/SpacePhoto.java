package com.paola.spacephotoapp;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpacePhoto {

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "link")
    private String link;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "guid")
    private String guid;

    @XmlElement(name = "pubDate")
    private String pubDate;

    private String imageUrl;
    private String localImagePath;

    // ----- Getters and Setters -----

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGuid() { return guid; }
    public void setGuid(String guid) { this.guid = guid; }

    public String getPubDate() { return pubDate; }
    public void setPubDate(String pubDate) { this.pubDate = pubDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocalImagePath() { return localImagePath; }
    public void setLocalImagePath(String localImagePath) { this.localImagePath = localImagePath; }

    @Override
    public String toString() {
        return title;
    }
}
