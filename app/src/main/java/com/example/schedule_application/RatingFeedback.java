package com.example.schedule_application;
public class RatingFeedback {

    private float rating;
    private String feedback;

    public RatingFeedback() {
        // Default constructor required for Firestore
    }

    public RatingFeedback(float rating, String feedback) {
        this.rating = rating;
        this.feedback = feedback;
    }

    public float getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }
}
