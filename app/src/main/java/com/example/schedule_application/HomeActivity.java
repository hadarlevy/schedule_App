package com.example.schedule_application;

import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity extends NavBarActivity {

    private static final String TAG = "HomeActivity";

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private LinearLayout shiftsContainer;
    private List<Shift> shiftList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

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
        shiftsContainer = findViewById(R.id.shiftsContainer);
        shiftList = new ArrayList<>();

        if (user != null) {
            tvUserEmail.setText(user.getEmail());
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
}
