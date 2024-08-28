package com.example.schedule_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditUserDetailsActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPhoneNumber;
    private Button btnSave;
    private ImageView backButton;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_details);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSave = findViewById(R.id.btnSave);
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

        if (user != null) {
            // Fetch user details from Firestore and populate EditText fields
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String firstName = documentSnapshot.getString("First name");
                                String lastName = documentSnapshot.getString("Last name");
                                String phoneNumber = documentSnapshot.getString("Phone");

                                etFirstName.setText(firstName);
                                etLastName.setText(lastName);
                                etPhoneNumber.setText(phoneNumber);
                            } else {
                                // Handle case where user document does not exist
                                Toast.makeText(EditUserDetailsActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditUserDetailsActivity.this, "Failed to fetch user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            // Handle case where user is null
            Toast.makeText(EditUserDetailsActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserDetails();
            }
        });
    }

    private void saveUserDetails() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (validateInput(firstName, lastName, phoneNumber)) {
            if (user != null) {
                DocumentReference userRef = db.collection("users").document(user.getUid());
                userRef.update("First name", firstName, "Last name", lastName, "Phone", phoneNumber)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditUserDetailsActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditUserDetailsActivity.this, "Error updating details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(EditUserDetailsActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean validateInput(String firstName, String lastName, String phoneNumber) {
        // Implement validation rules here
        // Example: check if fields are empty, validate format, etc.
        boolean isValid = true; // Implement your validation logic
        // Example validation
        if (firstName.isEmpty()) {
            etFirstName.setError("First name cannot be empty");
            isValid = false;
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Last name cannot be empty");
            isValid = false;
        }
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Phone number cannot be empty");
            isValid = false;
        }
        if (!firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Name must contain only English letters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!lastName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Name must contain only English letters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!phoneNumber.matches("\\d{10}")) {
            Toast.makeText(this, "Phone number must have exactly 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }


        return isValid;
    }
}




