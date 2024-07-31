// src/com/example/schedule_application/NavBarActivity.java
package com.example.schedule_application;

import static com.example.schedule_application.Registration.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class NavBarActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle toggle;
    protected Toolbar toolbar;
    protected FirebaseAuth auth;
    protected FirebaseFirestore db;
    protected FirebaseUser user;
    protected boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navbar); // Replace with your navigation drawer layout

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();
        isAdmin = false; // Initialize isAdmin to false initially

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.tool_bar);

        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Check if user is admin and update navigation view accordingly
        checkIfUserIsAdmin();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (isAdmin) {
            // Admin menu items
            if (id == R.id.nav_admin_home) {
                Intent intent = new Intent(this, AdminHomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_admin_schedule) {
                Intent intent = new Intent(this, AdminScheduleActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_support) {
                Intent intent = new Intent(this, SupportActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        } else {
            // Regular user menu items
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_schedule) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_my_timesheet) {
                Intent intent = new Intent(this, EmployeeTimesheetActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void checkIfUserIsAdmin() {
        if (user == null) {
            isAdmin = false;
            initializeNavigationView(); // Initialize navigation view immediately
            return;
        }

        DocumentReference docRef = db.collection("Manager").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // User is an admin
                        isAdmin = true;
                    } else {
                        // User is not an admin
                        isAdmin = false;
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
                initializeNavigationView(); // Initialize the navigation view based on user type
            }
        });
    }

    private void initializeNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateNavigationView();
    }

    private void updateNavigationView() {
        if (isAdmin) {
            navigationView.getMenu().clear(); // Clear existing menu items
            navigationView.inflateMenu(R.menu.admin_menu_navbar); // Inflate admin menu
        } else {
            navigationView.getMenu().clear(); // Clear existing menu items
            navigationView.inflateMenu(R.menu.drawer_menu); // Inflate regular user menu
        }
    }
}
