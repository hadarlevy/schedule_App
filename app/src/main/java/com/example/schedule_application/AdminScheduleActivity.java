package com.example.schedule_application;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminScheduleActivity extends NavBarActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    private LinearLayout step1Content, step2Content, step3Content;
    private List<EmployeePreference> employeePreferences;
    private Set<String> uniqueEmployees;

    private ImageView step1Icon, step2Icon, step3Icon;
    private View step1ToStep2Line, step2ToStep3Line;
    private RadioGroup shiftRadioGroup;
    private EditText fromDateEditText, toDateEditText;
    private Button createScheduleButton;
    private PopupWindow tooltipPopup;
    private TextView scheduleDate;
    private int shiftsPerDay = 1; // Default value
    private String startDate;
    private String endDate;
    private String weekAgoDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Initializing the views
        step1Content = findViewById(R.id.step1_content);
        step2Content = findViewById(R.id.step2_content);
        step3Content = findViewById(R.id.step3_content);
        step1Icon = findViewById(R.id.step1_icon);
        step2Icon = findViewById(R.id.step2_icon);
        step3Icon = findViewById(R.id.step3_icon);
        scheduleDate = findViewById(R.id.schedule_date);
        step1ToStep2Line = findViewById(R.id.step1_to_step2_line);
        step2ToStep3Line = findViewById(R.id.step2_to_step3_line);
        shiftRadioGroup = findViewById(R.id.shift_radio_group);
        createScheduleButton = findViewById(R.id.create_schedule_button);

        setScheduleDate();

        fetchEmployeePreferences();

        // Set onClickListeners for steps
        step1Icon.setOnClickListener(v -> showStepContent(1));
        step2Icon.setOnClickListener(v -> showStepContent(2));
        step3Icon.setOnClickListener(v -> showStepContent(3));

        // Set up tooltips for question marks
        setUpTooltips();

        // Create Schedule button functionality
        createScheduleButton.setOnClickListener(v -> createSchedule());
    }

    private void fetchEmployeePreferences() {
        uniqueEmployees = new HashSet<>();
        employeePreferences = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date startDateParsed = null;
        Date endDateParsed = null;
        try {
            startDateParsed = sdf.parse(startDate);
            endDateParsed = sdf.parse(endDate);

            // Adjust endDate to include the whole day
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDateParsed);
            cal.add(Calendar.DAY_OF_YEAR, 1); // Move to the next day
            endDateParsed = cal.getTime();
        } catch (ParseException e) {
            Log.e("FETCH_PREFS", "Date parsing failed", e);
            showToast("Failed to parse date.");
            return;
        }

        db.collection("shifts")
                .whereGreaterThanOrEqualTo("date", startDateParsed)
                .whereLessThan("date", endDateParsed) // Use less than the next day to include the whole end date
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, List<String>> possibleMap = new HashMap<>();
                        Map<String, List<String>> preferredMap = new HashMap<>();
                        Set<String> employeesWithPossibleShifts = new HashSet<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.getString("email");
                            Date date = document.getDate("date");
                            String option = document.getString("option");

                            Log.d("FETCH_PREFS", "Email: " + email + ", Date: " + date + ", Option: " + option);

                            // Skip "impossible" shifts
                            if ("Impossible".equals(option)) {
                                continue;
                            }

                            uniqueEmployees.add(email);

                            String dateString = sdf.format(date);
                            if ("Possible".equals(option)) {
                                if (!possibleMap.containsKey(email)) {
                                    possibleMap.put(email, new ArrayList<>());
                                }
                                possibleMap.get(email).add(dateString);
                            } else if ("Possible and Prefer".equals(option)) {
                                if (!preferredMap.containsKey(email)) {
                                    preferredMap.put(email, new ArrayList<>());
                                }
                                preferredMap.get(email).add(dateString);
                            }

                            // Mark employee as having at least one possible shift
                            employeesWithPossibleShifts.add(email);
                        }

                        for (String email : uniqueEmployees) {
                            if (!employeesWithPossibleShifts.contains(email)) {
                                continue; // Skip employees with only impossible shifts
                            }

                            List<String> possibleDays = possibleMap.containsKey(email) ? possibleMap.get(email) : new ArrayList<>();
                            List<String> preferredDays = preferredMap.containsKey(email) ? preferredMap.get(email) : new ArrayList<>();
                            employeePreferences.add(new EmployeePreference(email, possibleDays, preferredDays));
                        }

                        // Log the entire list of employee preferences
                        for (EmployeePreference preference : employeePreferences) {
                            Log.d("FETCH_PREFS", preference.toString());
                        }

                        Log.d("FETCH_PREFS", "Total unique employees: " + uniqueEmployees.size());

                        // Proceed with creating the schedule after fetching preferences
                        createSchedule();
                    } else {
                        showToast("Failed to fetch employee preferences.");
                    }
                });
    }

    private void showStepContent(int step) {
        resetSteps();

        switch (step) {
            case 1:
                step1Content.setVisibility(View.VISIBLE);
                break;
            case 2:
                step1Icon.setImageResource(R.drawable.ic_circle_fill1);
                step2Content.setVisibility(View.VISIBLE);
                animateLineFill(step1ToStep2Line, true);
                break;
            case 3:
                step1Icon.setImageResource(R.drawable.ic_circle_fill1);
                step2Icon.setImageResource(R.drawable.ic_circle_fill1);
                step3Icon.setImageResource(R.drawable.ic_circle_fill1);
                step3Content.setVisibility(View.VISIBLE);
                animateLineFill(step1ToStep2Line, true);
                animateLineFill(step2ToStep3Line, true);
                break;
        }
    }

    private void resetSteps() {
        step1Icon.setImageResource(R.drawable.ic_circle_hollow1);
        step2Icon.setImageResource(R.drawable.ic_circle_hollow2);
        step3Icon.setImageResource(R.drawable.ic_circle_hollow3);

        step1Content.setVisibility(View.GONE);
        step2Content.setVisibility(View.GONE);
        step3Content.setVisibility(View.GONE);

        step1ToStep2Line.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        step2ToStep3Line.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }

    private void animateLineFill(View line, boolean fill) {
        final int initialColor = getResources().getColor(android.R.color.darker_gray);
        final int finalColor = Color.parseColor("#4CB61A");

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int newColor = (Integer) new android.animation.ArgbEvaluator().evaluate(interpolatedTime, initialColor, finalColor);
                line.setBackgroundColor(newColor);
            }
        };
        animation.setDuration(1000);
        line.startAnimation(animation);
    }

    private void setScheduleDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 1); // Move to next week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Set to Sunday
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Move to Saturday of the same week
        endDate = sdf.format(calendar.getTime());

        // Calculate the date a week ago from startDate
        calendar.add(Calendar.DAY_OF_YEAR, -13); // -6 (for endDate) - 7 (one week before startDate)
        weekAgoDate = sdf.format(calendar.getTime());

        String dateRange = startDate + " - " + endDate;
        scheduleDate.setText(dateRange);

        Log.d("SCHEDULE_DATE", "Start date: " + startDate + ", End date: " + endDate + ", Week ago date: " + weekAgoDate);
    }

    private void setUpTooltips() {
        ImageView parameter1Info = findViewById(R.id.parameter1_info);
        ImageView parameter2Info = findViewById(R.id.parameter2_info);
        ImageView parameter3Info = findViewById(R.id.parameter3_info);

        parameter1Info.setOnClickListener(v -> showTooltip(v, "Ensures that all employees have a similar number of shifts. The goal is to keep the difference in the number of shifts between any two employees to a maximum of one."));
        parameter2Info.setOnClickListener(v -> showTooltip(v, "Evaluates how well the assigned shifts match what employees prefer, rewarding schedules that align with their desired shifts."));
        parameter3Info.setOnClickListener(v -> showTooltip(v, "Measures how well the schedule meets the required work hours by maximizing the number of covered shifts."));
    }

    private void showTooltip(View anchorView, String text) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.tooltip_layout, null);
        TextView tooltipText = popupView.findViewById(R.id.tooltip_text);
        tooltipText.setText(text);

        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }

        tooltipPopup = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        tooltipPopup.showAsDropDown(anchorView, 0, 0);

        // Dismiss the popup after a few seconds or when user clicks outside
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (tooltipPopup != null && tooltipPopup.isShowing()) {
                tooltipPopup.dismiss();
            }
        }, 3000);

        popupView.setOnTouchListener((v, event) -> {
            if (tooltipPopup != null && tooltipPopup.isShowing()) {
                tooltipPopup.dismiss();
            }
            return true;
        });
    }

    private void createSchedule() {
        // Validate step 1 selection
        int selectedId = shiftRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            // No selection in Step 1
            showToast("Please select shifts per day.");
            return;
        } else {
            // Update shiftsPerDay based on selection
            if (selectedId == R.id.radio_all_day) {
                shiftsPerDay = 1;
            } else if (selectedId == R.id.radio_morning_evening) {
                shiftsPerDay = 2;
            } else if (selectedId == R.id.radio_morning_noon_evening) {
                shiftsPerDay = 3;
            }
        }

        // Get selected ratings from Step 2
        int fairness = getSelectedRating(R.id.parameter1_rating);
        int preference = getSelectedRating(R.id.parameter2_rating);
        int cover = getSelectedRating(R.id.parameter3_rating);

        // Validate if any two ratings are the same
        if (fairness == preference || fairness == cover || preference == cover) {
            // Display error message about duplicate ratings
            showToast("Parameters cannot have the same rating.");
            return;
        }

        // Count the number of unique employees
        int numberOfEmployees = uniqueEmployees.size();
        Log.d("CREATE_SCHEDULE", "Number of unique employees: " + numberOfEmployees);
        showToast("Number of unique employees: " + numberOfEmployees);

        // Proceed with creating the schedule
        // Implement your scheduling logic here

        // Delete old shifts from the database
        deleteOldShifts();
    }

    private int getSelectedRating(int groupId) {
        RadioGroup group = findViewById(groupId);
        int selectedId = group.getCheckedRadioButtonId();

        if (selectedId == R.id.radio_fairness_1 || selectedId == R.id.radio_preferences_1 || selectedId == R.id.radio_employability_1) {
            return 1;
        } else if (selectedId == R.id.radio_fairness_2 || selectedId == R.id.radio_preferences_2 || selectedId == R.id.radio_employability_2) {
            return 2;
        } else if (selectedId == R.id.radio_fairness_3 || selectedId == R.id.radio_preferences_3 || selectedId == R.id.radio_employability_3) {
            return 3;
        } else {
            return 0;
        }
    }

    private void deleteOldShifts() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date weekAgoDateParsed = null;
        try {
            weekAgoDateParsed = sdf.parse(weekAgoDate);
        } catch (ParseException e) {
            Log.e("DELETE_OLD_SHIFTS", "Date parsing failed", e);
            showToast("Failed to parse week ago date.");
            return;
        }

        db.collection("shifts")
                .whereLessThan("date", weekAgoDateParsed)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();
                            db.collection("shifts").document(docId).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("DELETE_OLD_SHIFTS", "DocumentSnapshot successfully deleted: " + docId))
                                    .addOnFailureListener(e -> Log.w("DELETE_OLD_SHIFTS", "Error deleting document", e));
                        }
                        showToast("Old shifts deleted successfully.");
                    } else {
                        showToast("Failed to delete old shifts.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
