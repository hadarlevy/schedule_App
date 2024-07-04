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
                startActivity(new Intent(HomeActivity.this, EditUserDetailsActivity.class));
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

}

