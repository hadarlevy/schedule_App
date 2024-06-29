package com.example.schedule_application;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ViewShiftsActivity extends AppCompatActivity {

    private LinearLayout shiftsContainer;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shifts);

        shiftsContainer = findViewById(R.id.shiftsContainer);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        loadCurrentWeekShifts();
    }

    private void loadCurrentWeekShifts() {
        if (user != null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Get start and end dates of the current week
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            String startDate = sdf.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            String endDate = sdf.format(calendar.getTime());

            db.collection("shifts")
                    .whereEqualTo("email", user.getEmail())
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        shiftsContainer.removeAllViews();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Shift shift = documentSnapshot.toObject(Shift.class);
                            addShiftView(shift);
                        }
                    });
        }
    }

    private void addShiftView(Shift shift) {
        TextView shiftView = new TextView(this);
        shiftView.setText(String.format("Shift: %s\nDate: %s", shift.getOption(), shift.getDate()));
        shiftView.setPadding(16, 16, 16, 16);
        shiftView.setBackgroundResource(R.color.gray);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16, 16, 16);
        shiftView.setLayoutParams(params);
        shiftsContainer.addView(shiftView);
    }
}
