package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewShiftsActivity extends AppCompatActivity {

    private static final String TAG = "ViewShiftsActivity";

    private LinearLayout shiftsContainer;
    private List<Shift> shiftList;
    private String userEmail;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shifts);

        shiftsContainer = findViewById(R.id.shiftsContainer);
        backButton = findViewById(R.id.backButton);
        shiftList = new ArrayList<>();

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null) {
            loadUserShifts(userEmail);
        } else {
            Log.d(TAG, "User email is null");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadUserShifts(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("shifts")
                .whereEqualTo("email", email)
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
                            showNoShiftsMessage();
                            Log.d(TAG, "No shifts found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewShiftsActivity.this, "Error loading shifts", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error loading shifts", e);
                    }
                });
    }

    private void addShiftView(Shift shift, int shiftNumber) {
        View shiftView = LayoutInflater.from(this).inflate(R.layout.shift_item, shiftsContainer, false);

        TextView shiftNoView = shiftView.findViewById(R.id.shift_no);
        TextView shiftDateView = shiftView.findViewById(R.id.shift_date);

        shiftNoView.setText("Shift no: " + shiftNumber);
        shiftDateView.setText(shift.getDate());

        shiftsContainer.addView(shiftView);
    }

    private void showNoShiftsMessage() {
        TextView noShiftsView = new TextView(this);
        noShiftsView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        noShiftsView.setText("There are no shifts for the week.");
        noShiftsView.setTextSize(18);
        noShiftsView.setGravity(Gravity.CENTER);
        shiftsContainer.addView(noShiftsView);
    }
}
