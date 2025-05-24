package com.example.communityapp;

public class Event {
    private String id;
    private String title;
    private String date;
    private String location;
    private int image;        // Drawable resource ID
    private String category;
    private String description;

    // Constructor including category
    public Event(String id, String title, String date, String location, int image, String category, String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.image = image;
        this.category = category;
        this.description = description;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;

    }

    public String getLocation() {
        return location;
    }

    public int getImage() {
        return image;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
