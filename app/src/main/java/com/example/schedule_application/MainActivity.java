package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    MaterialCalendarView calendarView;
    TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        calendarView = findViewById(R.id.calendarView);
        selectedDate = findViewById(R.id.selectedDate);

        // Set up calendar view
        calendarView.setSelectedDate(CalendarDay.today());
        highlightCurrentWeek();

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDate.setText("Selected Date: " + date.getDate().toString());
                showPopup(date);
            }
        });

        if (user != null && !user.isEmailVerified()) {
            showEmailVerificationDialog();
        }
    }

    private void highlightCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int startOfWeek = calendar.getFirstDayOfWeek();

        calendar.add(Calendar.DAY_OF_YEAR, startOfWeek - dayOfWeek);

        CalendarDay firstDay = CalendarDay.from(calendar);
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        CalendarDay lastDay = CalendarDay.from(calendar);

        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        calendarView.setDateSelected(firstDay, true);
        calendarView.setDateSelected(lastDay, true);
    }

    private void showPopup(final CalendarDay date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option for " + date.getDate().toString());

        String[] options = {"Possible and Prefer", "Possible", "Impossible"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which];
                Toast.makeText(MainActivity.this, selectedOption + " selected for " + date.getDate().toString(), Toast.LENGTH_SHORT).show();
                // Here you can update the UI or save the selection in the database
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEmailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your email has not been verified. Would you like to verify it now?")
                .setCancelable(false)
                .setPositiveButton("Verify Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendVerificationEmail();
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendVerificationEmail() {
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
}
