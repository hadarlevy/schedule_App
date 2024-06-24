package com.example.schedule_application;

public class RatingFeedback {

    private String email;
    private float rating;
    private String feedback;

    public RatingFeedback() {
        // Default constructor required for Firestore
    }

    public RatingFeedback(String email, float rating, String feedback) {
        this.email = email;
        this.rating = rating;
        this.feedback = feedback;
    }

    public String getEmail() {
        return email;
    }

    public float getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }
}
