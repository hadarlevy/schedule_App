package com.example.schedule_application;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EmployeeTimesheetActivity extends NavBarActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    private TextView tvDateRange;
    private TextView tvSunShift, tvMonShift, tvTueShift, tvWedShift, tvThuShift, tvFriShift, tvSatShift;

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
        tvSunShift = findViewById(R.id.tvSunShift);
        tvMonShift = findViewById(R.id.tvMonShift);
        tvTueShift = findViewById(R.id.tvTueShift);
        tvWedShift = findViewById(R.id.tvWedShift);
        tvThuShift = findViewById(R.id.tvThuShift);
        tvFriShift = findViewById(R.id.tvFriShift);
        tvSatShift = findViewById(R.id.tvSatShift);

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
        // For demonstration purposes, we'll just set some example shifts
        // Replace this with your actual shift data

        tvSunShift.setText("Morning");
        tvMonShift.setText("Evening");
        tvTueShift.setText("-");
        tvWedShift.setText("Morning");
        tvThuShift.setText("-");
        tvFriShift.setText("Evening");
        tvSatShift.setText("Morning");
    }
}
