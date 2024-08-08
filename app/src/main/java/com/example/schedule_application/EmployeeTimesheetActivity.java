package com.example.schedule_application;

import android.os.Bundle;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeTimesheetActivity extends NavBarActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    private TextView tvDateRange;
    private Map<String, Map<String, TextView>> dayShiftMap;
    private FirebaseFirestore db;
    private String userEmail;
    private TableRow rowSun, rowMon, rowTue, rowWed, rowThu, rowFri, rowSat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_timesheet);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        tvDateRange = findViewById(R.id.tvDateRange);

        // Initialize dayShiftMap with TextViews
        dayShiftMap = new HashMap<>();
        dayShiftMap.put("Sunday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvSunMorningShift));
            put("Noon", findViewById(R.id.tvSunNoonShift));
            put("Evening", findViewById(R.id.tvSunEveningShift));
        }});
        dayShiftMap.put("Monday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvMonMorningShift));
            put("Noon", findViewById(R.id.tvMonNoonShift));
            put("Evening", findViewById(R.id.tvMonEveningShift));
        }});
        dayShiftMap.put("Tuesday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvTueMorningShift));
            put("Noon", findViewById(R.id.tvTueNoonShift));
            put("Evening", findViewById(R.id.tvTueEveningShift));
        }});
        dayShiftMap.put("Wednesday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvWedMorningShift));
            put("Noon", findViewById(R.id.tvWedNoonShift));
            put("Evening", findViewById(R.id.tvWedEveningShift));
        }});
        dayShiftMap.put("Thursday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvThuMorningShift));
            put("Noon", findViewById(R.id.tvThuNoonShift));
            put("Evening", findViewById(R.id.tvThuEveningShift));
        }});
        dayShiftMap.put("Friday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvFriMorningShift));
            put("Noon", findViewById(R.id.tvFriNoonShift));
            put("Evening", findViewById(R.id.tvFriEveningShift));
        }});
        dayShiftMap.put("Saturday", new HashMap<String, TextView>() {{
            put("Morning", findViewById(R.id.tvSatMorningShift));
            put("Noon", findViewById(R.id.tvSatNoonShift));
            put("Evening", findViewById(R.id.tvSatEveningShift));
        }});

        // Initialize TableRow variables
        rowSun = findViewById(R.id.rowSun);
        rowMon = findViewById(R.id.rowMon);
        rowTue = findViewById(R.id.rowTue);
        rowWed = findViewById(R.id.rowWed);
        rowThu = findViewById(R.id.rowThu);
        rowFri = findViewById(R.id.rowFri);
        rowSat = findViewById(R.id.rowSat);

        db = FirebaseFirestore.getInstance();
        userEmail = user.getEmail(); // Ensure you pass the email when starting this activity

        setScheduleDate();
        populateShifts();
    }

    private void setScheduleDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 1); // Move to next week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to Sunday
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Move to Saturday of the same week
        String endDate = sdf.format(calendar.getTime());

        String dateRange = startDate + " - " + endDate;
        tvDateRange.setText(dateRange);
    }

    private void populateShifts() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 1); // Move to next week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to Sunday
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String date = sdf.format(calendar.getTime());
            final String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            final TableRow currentRow = getTableRow(dayOfWeek);

            db.collection("schedule")
                    .whereEqualTo("Email", userEmail)
                    .whereEqualTo("Date", date)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            boolean allDay = true;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String morning = document.getString("Morning");
                                String noon = document.getString("Noon");
                                String evening = document.getString("Evening");

                                Map<String, TextView> shiftTextViews = dayShiftMap.get(dayOfWeek);

                                if ("yes".equals(morning)) {
                                    shiftTextViews.get("Morning").setBackgroundColor(getResources().getColor(R.color.yellow));
                                    shiftTextViews.get("Morning").setText("working");
                                } else {
                                    allDay = false;
                                }
                                if ("yes".equals(noon)) {
                                    shiftTextViews.get("Noon").setBackgroundColor(getResources().getColor(R.color.yellow));
                                    shiftTextViews.get("Noon").setText("working");
                                } else {
                                    allDay = false;
                                }
                                if ("yes".equals(evening)) {
                                    shiftTextViews.get("Evening").setBackgroundColor(getResources().getColor(R.color.yellow));
                                    shiftTextViews.get("Evening").setText("working");
                                } else {
                                    allDay = false;
                                }
                            }
                            if (allDay) {
                                currentRow.setBackgroundColor(getResources().getColor(R.color.yellow));
                                for (TextView shiftView : dayShiftMap.get(dayOfWeek).values()) {
                                    shiftView.setText("All day");
                                }
                            }
                        } else {
                            for (TextView shiftView : dayShiftMap.get(dayOfWeek).values()) {
                                shiftView.setText("-");
                            }
                        }
                    });

            calendar.add(Calendar.DAY_OF_WEEK, 1); // Move to next day
        }
    }

    private TableRow getTableRow(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Sunday":
                return rowSun;
            case "Monday":
                return rowMon;
            case "Tuesday":
                return rowTue;
            case "Wednesday":
                return rowWed;
            case "Thursday":
                return rowThu;
            case "Friday":
                return rowFri;
            case "Saturday":
                return rowSat;
            default:
                return null;
        }
    }
}
