package com.example.communityapp;

public class Volunteer {
    private String volunteerId;
    private String fullName;
    private String username;
    private String phone;

    public Volunteer(String volunteerId, String fullName, String username, String phone) {
        this.volunteerId = volunteerId;
        this.fullName = fullName;
        this.username = username;
        this.phone = phone;
    }

    // Getters
    public String getVolunteerId() {
        return volunteerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;

    }

    public String getPhone() {
        return phone;
    }
}