package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeActivity extends NavBarActivity {

    private static final String TAG = "AdminHomeActivity";
    private static final int EDIT_DETAILS_REQUEST = 1;

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private EditText etUserName, etUserPhone;
    private TextView editUserDetailsLink, viewInfoLink, rateUsLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

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
        editUserDetailsLink = findViewById(R.id.editUserDetailsLink);
        viewInfoLink = findViewById(R.id.viewShiftsLink);
        rateUsLink = findViewById(R.id.rateUsLink);

        editUserDetailsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, EditAdminDetailsActivity.class);
                startActivityForResult(intent, EDIT_DETAILS_REQUEST);
            }
        });

        viewInfoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, InformationActivity.class));
            }
        });

        rateUsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, RateUsActivity.class));
            }
        });

        loadUserDetails();
    }

    private void loadUserDetails() {
        if (user != null) {
            tvUserEmail.setText(user.getEmail());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Manager").document(user.getUid()).get()
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_DETAILS_REQUEST && resultCode == RESULT_OK) {
            loadUserDetails();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
