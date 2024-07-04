package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends NavBarActivity {

    private static final String TAG = "HomeActivity";

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private EditText etUserName, etUserPhone;
    private LinearLayout shiftsContainer;
    private TextView editUserDetailsLink, saveUserDetailsLink, viewShiftsLink, rateUsLink;
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
        editUserDetailsLink = findViewById(R.id.editUserDetailsLink);
        saveUserDetailsLink = findViewById(R.id.saveUserDetailsLink);
        viewShiftsLink = findViewById(R.id.viewShiftsLink);
        rateUsLink = findViewById(R.id.rateUsLink);
        shiftList = new ArrayList<>();

        editUserDetailsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode(true);
            }
        });

        saveUserDetailsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserDetails();
            }
        });

        viewShiftsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ViewShiftsActivity.class);
                if (user != null) {
                    intent.putExtra("USER_EMAIL", user.getEmail());
                }
                startActivity(intent);
            }
        });

        rateUsLink.setOnClickListener(new View.OnClickListener() {
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

        } else {
            Log.d(TAG, "User is null");
        }
    }

    private void toggleEditMode(boolean editMode) {
        if (editMode) {
            tvUserName.setVisibility(View.GONE);
            tvUserPhone.setVisibility(View.GONE);
            etUserName.setVisibility(View.VISIBLE);
            etUserPhone.setVisibility(View.VISIBLE);
            saveUserDetailsLink.setVisibility(View.VISIBLE);
            editUserDetailsLink.setVisibility(View.GONE);
            viewShiftsLink.setVisibility(View.GONE);
            rateUsLink.setVisibility(View.GONE);
        } else {
            tvUserName.setVisibility(View.VISIBLE);
            tvUserPhone.setVisibility(View.VISIBLE);
            etUserName.setVisibility(View.GONE);
            etUserPhone.setVisibility(View.GONE);
            saveUserDetailsLink.setVisibility(View.GONE);
            editUserDetailsLink.setVisibility(View.VISIBLE);
            viewShiftsLink.setVisibility(View.VISIBLE);
            rateUsLink.setVisibility(View.VISIBLE);
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