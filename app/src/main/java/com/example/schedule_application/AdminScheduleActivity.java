package com.example.schedule_application;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminScheduleActivity extends NavBarActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    private LinearLayout step1Content, step2Content, step3Content;
    private ImageView step1Icon, step2Icon, step3Icon;
    private View step1ToStep2Line, step2ToStep3Line;
    private RadioGroup shiftRadioGroup;
    private EditText fromDateEditText, toDateEditText;
    private Button createScheduleButton;
    private PopupWindow tooltipPopup;
    private TextView scheduleDate;
    private int shiftsPerDay = 1; // Default value

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

        // Set onClickListeners for steps
        step1Icon.setOnClickListener(v -> showStepContent(1));
        step2Icon.setOnClickListener(v -> showStepContent(2));
        step3Icon.setOnClickListener(v -> showStepContent(3));

        // Set up tooltips for question marks
        setUpTooltips();

        // Create Schedule button functionality
        createScheduleButton.setOnClickListener(v -> createSchedule());
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

        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Move to Saturday of the same week
        String endDate = sdf.format(calendar.getTime());

        String dateRange = startDate + " - " + endDate;
        scheduleDate.setText(dateRange);
    }

    private void setUpTooltips() {
        ImageView parameter1Info = findViewById(R.id.parameter1_info);
        ImageView parameter2Info = findViewById(R.id.parameter2_info);
        ImageView parameter3Info = findViewById(R.id.parameter3_info);

        parameter1Info.setOnClickListener(v -> showTooltip(v, " Ensures that all employees have a similar number of shifts. The goal is to keep the difference in the number of shifts between any two employees to a maximum of one."));
        parameter2Info.setOnClickListener(v -> showTooltip(v, "Evaluates how well the assigned shifts match what employees prefer, rewarding schedules that align with their desired shifts."));
        parameter3Info.setOnClickListener(v -> showTooltip(v, " Measures how well the schedule meets the required work hours by maximizing the number of covered shifts."));
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

        // Proceed with creating the schedule
        // You can implement your scheduling logic here
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
