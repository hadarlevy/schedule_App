package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends NavBarActivity {

    private static final String TAG = "HomeActivity";

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private EditText etUserName, etUserPhone;
    private LinearLayout shiftsContainer;
    private Button editUserDetailsButton, saveUserDetailsButton, viewShiftsButton, rateUsButton;
    private List<Shift> shiftList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        etUserName = findViewById(R.id.etUserName);
        etUserPhone = findViewById(R.id.etUserPhone);
        shiftsContainer = findViewById(R.id.shiftsContainer);
        editUserDetailsButton = findViewById(R.id.editUserDetailsButton);
        saveUserDetailsButton = findViewById(R.id.saveUserDetailsButton);
        viewShiftsButton = findViewById(R.id.viewShiftsButton);
        rateUsButton = findViewById(R.id.rateUsButton);
        shiftList = new ArrayList<>();

        editUserDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode(true);
            }
        });

        saveUserDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserDetails();
            }
        });

        viewShiftsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ViewShiftsActivity.class));
            }
        });

        rateUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, RateUsActivity.class));
            }
        });

        if (user != null) {
            tvUserEmail.setText(user.getEmail());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String firstName = documentSnapshot.getString("First name");
                                String lastName = documentSnapshot.getString("Last name");
                                String phoneNumber = documentSnapshot.getString("Phone");
                                String displayName = firstName + " " + lastName;
                                tvUserName.setText(displayName);
                                tvUserPhone.setText(phoneNumber);
                                etUserName.setText(displayName);
                                etUserPhone.setText(phoneNumber);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "get failed with ", e);
                        }
                    });

            loadUserShifts();
        } else {
            Log.d(TAG, "User is null");
        }
    }

    private void loadUserShifts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("shifts")
                .whereEqualTo("email", user.getEmail())
                .whereIn("option", Arrays.asList("Possible", "Possible and Prefer"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        shiftList.clear();
                        shiftsContainer.removeAllViews();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int shiftNumber = 1;
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Shift shift = documentSnapshot.toObject(Shift.class);
                                shiftList.add(shift);
                                addShiftView(shift, shiftNumber);
                                shiftNumber++;
                            }
                        } else {
                            Log.d(TAG, "No shifts found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Error loading shifts", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error loading shifts", e);
                    }
                });
    }

    private void addShiftView(Shift shift, int shiftNumber) {
        // Create a LinearLayout to hold the shift details
        LinearLayout shiftLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(32, 0, 32, 16); // Adjusting margins to center the item and add spacing

        shiftLayout.setLayoutParams(layoutParams);
        shiftLayout.setOrientation(LinearLayout.VERTICAL);
        shiftLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        shiftLayout.setPadding(16, 16, 16, 16);
        shiftLayout.setBackgroundColor(getResources().getColor(R.color.gray));

        // Create and add TextView for shift number
        TextView shiftNoView = new TextView(this);
        shiftNoView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        shiftNoView.setText("Shift No. " + shiftNumber);
        shiftNoView.setTextSize(18);
        shiftNoView.setGravity(Gravity.CENTER);

        // Create and add TextView for shift date
        TextView shiftDateView = new TextView(this);
        shiftDateView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        shiftDateView.setText(shift.getDate());
        shiftDateView.setTextSize(14);
        shiftDateView.setGravity(Gravity.CENTER);

        // Add TextViews to shiftLayout
        shiftLayout.addView(shiftNoView);
        shiftLayout.addView(shiftDateView);

        // Add shiftLayout to shiftsContainer
        shiftsContainer.addView(shiftLayout);
    }

    private void toggleEditMode(boolean editMode) {
        if (editMode) {
            tvUserName.setVisibility(View.GONE);
            tvUserPhone.setVisibility(View.GONE);
            etUserName.setVisibility(View.VISIBLE);
            etUserPhone.setVisibility(View.VISIBLE);
            saveUserDetailsButton.setVisibility(View.VISIBLE);
            editUserDetailsButton.setVisibility(View.GONE);
            viewShiftsButton.setVisibility(View.GONE);
            rateUsButton.setVisibility(View.GONE);
        } else {
            tvUserName.setVisibility(View.VISIBLE);
            tvUserPhone.setVisibility(View.VISIBLE);
            etUserName.setVisibility(View.GONE);
            etUserPhone.setVisibility(View.GONE);
            saveUserDetailsButton.setVisibility(View.GONE);
            editUserDetailsButton.setVisibility(View.VISIBLE);
            viewShiftsButton.setVisibility(View.VISIBLE);
            rateUsButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveUserDetails() {
        String newName = etUserName.getText().toString().trim().replaceAll("\\s+", " ");
        String newPhone = etUserPhone.getText().toString().trim();

        if (validateName(newName) && validatePhone(newPhone)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid())
                    .update("First name", getFirstName(newName), "Last name", getLastName(newName), "Phone", newPhone)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toggleEditMode(false);
                            Toast.makeText(HomeActivity.this, "Details updated successfully", Toast.LENGTH_SHORT).show();
                            tvUserName.setText(newName);
                            tvUserPhone.setText(newPhone);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, "Failed to update details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Error updating details", e);
                        }
                    });
        }
    }

    private boolean validateName(String name) {
        // Validate name contains only English letters and exactly one space separating first name and last name
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!name.contains(" ")) {
            Toast.makeText(this, "Please provide both first and last name separated by a space", Toast.LENGTH_SHORT).show();
            return false;
        }

        String[] parts = name.split(" ");
        if (parts.length != 2) {
            Toast.makeText(this, "Name should only contain one space", Toast.LENGTH_SHORT).show();
            return false;
        }

        String firstName = parts[0];
        String lastName = parts[1];

        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Name must contain only English letters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validatePhone(String phone) {
        // Validate phone number has exactly 10 digits
        if (!phone.matches("\\d{10}")) {
            Toast.makeText(this, "Phone number must have exactly 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private String getFirstName(String fullName) {
        int index = fullName.indexOf(' ');
        return (index == -1) ? fullName : fullName.substring(0, index);
    }

    private String getLastName(String fullName) {
        int index = fullName.indexOf(' ');
        return (index == -1) ? "" : fullName.substring(index + 1);
    }
}
