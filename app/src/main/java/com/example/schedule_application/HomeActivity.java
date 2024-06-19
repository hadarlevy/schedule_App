package com.example.schedule_application;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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

public class HomeActivity extends NavBarActivity {

    private TextView tvUserName, tvUserEmail;
    private ListView listViewShifts;
    private ShiftsAdapter shiftsAdapter;
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
        listViewShifts = findViewById(R.id.listViewShifts);
        shiftList = new ArrayList<>();
        shiftsAdapter = new ShiftsAdapter(this, shiftList);
        listViewShifts.setAdapter(shiftsAdapter);

        if (user != null) {
            tvUserEmail.setText(user.getEmail());
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String firstName = documentSnapshot.getString("First name");
                                String lastName = documentSnapshot.getString("Last name");
                                String displayName = firstName + " " + lastName;
                                tvUserName.setText(displayName);
                            }
                        }
                    });

            loadUserShifts();
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
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Shift shift = documentSnapshot.toObject(Shift.class);
                            shiftList.add(shift);
                        }
                        shiftsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Error loading shifts", Toast.LENGTH_SHORT).show();
                        Log.d("HomeActivity", "Error loading shifts", e);
                    }
                });
    }
}
