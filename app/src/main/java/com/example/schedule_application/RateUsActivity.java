package com.example.schedule_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RateUsActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText feedbackEditText;
    private Button submitButton;
    private ImageButton backButton;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rating_activity);

        ratingBar = findViewById(R.id.ratingBar);
        feedbackEditText = findViewById(R.id.feedbackEditText);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                String feedback = feedbackEditText.getText().toString().trim();

                if (user != null && user.getEmail() != null) {
                    String email = user.getEmail();
                    saveRatingAndFeedback(email, rating, feedback);
                    Toast.makeText(RateUsActivity.this, "Rating and feedback submitted. Thank you!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RateUsActivity.this, "User not logged in or email not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveRatingAndFeedback(String email, float rating, String feedback) {
        if (user != null) {
            DocumentReference userRatingRef = db.collection("ratings").document(user.getUid());
            RatingFeedback ratingFeedback = new RatingFeedback(email, rating, feedback);

            userRatingRef.set(ratingFeedback)
                    .addOnSuccessListener(aVoid -> {
                        // Rating and feedback saved successfully
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RateUsActivity.this, "Failed to save rating and feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}