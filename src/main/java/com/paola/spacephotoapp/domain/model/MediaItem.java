package com.paola.spacephotoapp.domain.model;

/**
 * This is an abstract base class that represents a generic media item.
 * It provides common properties like a title and a description that are shared
 * by different types of media content (e.g., news articles, photos, videos).
 * Subclasses will inherit these properties and can add their own specific details.
 */
public abstract class MediaItem {
    private String title;       // The title of the media item.
    private String description; // A brief description of the media item.

    // --- Getters and Setters ---
    // These methods allow other parts of the application to access and modify
    // the title and description of a MediaItem.

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}