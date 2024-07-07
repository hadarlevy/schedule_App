package com.example.schedule_application;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Arrays;
import java.util.List;

public class InformationActivity extends NavBarActivity {

    private static final String TAG = "InformationActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private LinearLayout llEmployees, llShifts;
    private TextView tvSchedulesCount, tvShiftsHeader;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_activity);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        llEmployees = findViewById(R.id.llEmployees);
        llShifts = findViewById(R.id.llShifts);
        tvSchedulesCount = findViewById(R.id.tvSchedulesCount);
        tvShiftsHeader = findViewById(R.id.tvShiftsHeader);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        db = FirebaseFirestore.getInstance();

        fetchEmployees();
        fetchSchedulesCount();
    }

    private void fetchEmployees() {
        CollectionReference usersRef = db.collection("users");
        usersRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot document : documents) {
                            String fname = document.getString("First name");
                            String lname = document.getString("Last name");
                            String name = fname + " " + lname;
                            String phone = document.getString("Phone");
                            String email = document.getString("Email");
                            addEmployeeInfo(name, phone, email);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching users: ", e);
                    }
                });
    }

    private void addEmployeeInfo(final String name, final String phone, final String email) {
        // Create a new LinearLayout for each employee
        LinearLayout employeeLayout = new LinearLayout(this);
        employeeLayout.setOrientation(LinearLayout.VERTICAL);
        employeeLayout.setPadding(16, 16, 16, 16);
        employeeLayout.setBackgroundResource(R.drawable.shadow); // Example background
        employeeLayout.setGravity(Gravity.CENTER); // Center the content horizontally

        // Create TextViews for name and phone
        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        nameTextView.setTextSize(16);
        nameTextView.setPadding(0, 0, 0, 8);

        TextView phoneTextView = new TextView(this);
        phoneTextView.setText(phone);
        phoneTextView.setTextSize(14);

        // Add TextViews to the employee layout
        employeeLayout.addView(nameTextView);
        employeeLayout.addView(phoneTextView);

        // Set onClickListener to fetch shifts for the employee
        employeeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchShiftsForEmployee(email);
            }
        });

        // Add the employee layout to the main employees LinearLayout
        llEmployees.addView(employeeLayout);
    }

    private void fetchShiftsForEmployee(String email) {
        CollectionReference shiftsRef = db.collection("shifts");
        shiftsRef.whereEqualTo("email", email)
                .whereIn("option", Arrays.asList("Possible", "Possible and Prefer"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        llShifts.removeAllViews();
                        tvShiftsHeader.setVisibility(View.VISIBLE);
                        if (queryDocumentSnapshots.isEmpty()) {
                            TextView noShiftsTextView = new TextView(InformationActivity.this);
                            noShiftsTextView.setText("No shifts yet");
                            noShiftsTextView.setPadding(16, 16, 16, 16);
                            llShifts.addView(noShiftsTextView);
                        } else {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                String shiftDate = document.getString("date");
                                addShiftInfo(shiftDate);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching shifts: ", e);
                    }
                });
    }

    private void addShiftInfo(String shiftDate) {
        TextView textView = new TextView(this);
        textView.setText("Shift Date: " + shiftDate);
        textView.setPadding(16, 16, 16, 16);
        llShifts.addView(textView);
    }

    private void fetchSchedulesCount() {
        CollectionReference schedulesRef = db.collection("schedule");
        schedulesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int schedulesCount = queryDocumentSnapshots.size();
                        if (schedulesCount > 0) {
                            tvSchedulesCount.setText("Total Schedules: " + schedulesCount);
                        } else {
                            tvSchedulesCount.setText("No schedules have been created yet.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching schedules: ", e);
                    }
                });
    }
}
