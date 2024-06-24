package com.example.schedule_application;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends NavBarActivity {

    FirebaseAuth auth;
    CalendarView calendarView;
    FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); // Set the correct layout here

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        calendarView = findViewById(R.id.calendarView);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser(); // current user

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            reloadUserAndCheckEmailVerification();
        }

        calendarView.setDate(Calendar.getInstance().getTimeInMillis(), false, true);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                showShiftBottomSheetDialog(selectedDate);
            }
        });
    }

    private void reloadUserAndCheckEmailVerification() {
        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (!user.isEmailVerified()) {
                        showVerifyEmailDialog();
                    }
                } else {
                    Log.e("MainActivity", "Failed to reload user.", task.getException());
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showVerifyEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification")
                .setMessage("Your email is not verified. Please verify your email to continue.")
                .setPositiveButton("Verify Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MainActivity.this, "Verification Email Has been Sent", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("tag", "OnFailure: Email not sent " + e.getMessage());
                            }
                        });
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showShiftBottomSheetDialog(Calendar date) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_shift_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView tvTitle = bottomSheetView.findViewById(R.id.tv_shift_dialog_title);
        tvTitle.setText("Set Shift for " + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date.getTime()));

        RadioGroup rgShiftOptions = bottomSheetView.findViewById(R.id.rg_shift_options);

        bottomSheetView.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = rgShiftOptions.getCheckedRadioButtonId();
                String selectedOption = "Unknown";

                if (selectedId == R.id.rb_possible_and_prefer) {
                    selectedOption = "Possible and Prefer";
                } else if (selectedId == R.id.rb_possible) {
                    selectedOption = "Possible";
                } else if (selectedId == R.id.rb_impossible) {
                    selectedOption = "Impossible";
                }

                // Save the selected option to Fire store
                saveShiftOptionToDatabase(user.getEmail(), date, selectedOption);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void saveShiftOptionToDatabase(String email, Calendar date, String option) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        DocumentReference docRef = db.collection("shifts").document(email + "_" + formattedDate);

        Map<String, Object> shiftData = new HashMap<>();
        shiftData.put("email", email);
        shiftData.put("date", formattedDate);
        shiftData.put("option", option);

        docRef.set(shiftData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Shift option saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error saving shift option", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "Error saving shift option", e);
                    }
                });
    }
}
