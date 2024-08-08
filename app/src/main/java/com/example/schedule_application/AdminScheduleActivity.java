package com.example.schedule_application;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMultipart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import org.apache.poi.ss.usermodel.*;
import java.util.concurrent.ExecutionException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AdminScheduleActivity extends NavBarActivity {
    private List<Map<String, String>> scheduleData;

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
        db.collection("schedule")
                .whereLessThan("date", weekAgoDateParsed)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docId = document.getId();
                            db.collection("schedule").document(docId).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("DELETE_OLD_SCHEDULES", "DocumentSnapshot successfully deleted: " + docId))
                                    .addOnFailureListener(e -> Log.w("DELETE_OLD_SCHEDULES", "Error deleting document", e));
                        }
                        showToast("Old schedule deleted successfully.");
                    } else {
                        showToast("Failed to delete old schedule.");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void createSchedule() {
        // Validate step 1 selection
        int selectedId = shiftRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            showToast("Please select shifts per day.");
            return;
        } else {
            if (selectedId == R.id.radio_all_day) {
                shiftsPerDay = 1;
            } else if (selectedId == R.id.radio_morning_evening) {
                shiftsPerDay = 2;
            } else if (selectedId == R.id.radio_morning_noon_evening) {
                shiftsPerDay = 3;
            }
        }

        int fairness = getSelectedRating(R.id.parameter1_rating);
        int preference = getSelectedRating(R.id.parameter2_rating);
        int cover = getSelectedRating(R.id.parameter3_rating);

        if (fairness == preference || fairness == cover || preference == cover) {
            showToast("Parameters cannot have the same rating.");
            return;
        }

        showToast("This may take a few moments...");
        int numberOfEmployees = uniqueEmployees.size();
        Log.d("CREATE_SCHEDULE", "Number of unique employees: " + numberOfEmployees);
        showToast("Number of unique employees: " + numberOfEmployees);

        deleteOldShifts();

        Map<String, Object> result = ScheduleAlgorithm.geneticAlgorithm(
                employeePreferences,
                uniqueEmployees,
                fairness,
                cover,
                preference,
                shiftsPerDay
        );

        String bestSchedule = (String) result.get("bestSchedule");
        Log.d("CREATE_SCHEDULE", "Best Schedule: " + bestSchedule);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date startDateParsed = null;
        Date endDateParsed = null;
        try {
            startDateParsed = sdf.parse(startDate);
            endDateParsed = sdf.parse(endDate);
        } catch (ParseException e) {
            Log.e("CREATE_SCHEDULE", "Date parsing failed", e);
            showToast("Failed to parse dates.");
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDateParsed);
        int numDays = (int) ((endDateParsed.getTime() - startDateParsed.getTime()) / (1000 * 60 * 60 * 24)) + 1;

        for (int dayOffset = 0; dayOffset < numDays; dayOffset++) {
            cal.setTime(startDateParsed);
            cal.add(Calendar.DAY_OF_YEAR, dayOffset);
            Date scheduleDate = cal.getTime();
            String dateString = sdf.format(scheduleDate);

            for (String email : uniqueEmployees) {
                int employeeIndex = new ArrayList<>(uniqueEmployees).indexOf(email);
                for (int shiftIndex = 0; shiftIndex < shiftsPerDay; shiftIndex++) {
                    int bitIndex = employeeIndex * shiftsPerDay + dayOffset * shiftsPerDay + shiftIndex;
                    boolean isShift = bestSchedule.charAt(bitIndex) == '1';
                    if (isShift) {
                        Map<String, Object> shiftData = new HashMap<>();
                        shiftData.put("Email", email);
                        shiftData.put("Date", dateString);

                        if (shiftsPerDay == 1) {
                            shiftData.put("Morning", "yes");
                            shiftData.put("Noon", "yes");
                            shiftData.put("Evening", "yes");
                        } else if (shiftsPerDay == 2) {
                            shiftData.put("Morning", (shiftIndex == 0) ? "yes" : "no");
                            shiftData.put("Noon", "no");
                            shiftData.put("Evening", (shiftIndex == 1) ? "yes" : "no");
                        } else if (shiftsPerDay == 3) {
                            shiftData.put("Morning", (shiftIndex == 0) ? "yes" : "no");
                            shiftData.put("Noon", (shiftIndex == 1) ? "yes" : "no");
                            shiftData.put("Evening", (shiftIndex == 2) ? "yes" : "no");
                        }

                        // Check if the exact shift already exists
                        db.collection("schedule")
                                .whereEqualTo("Email", email)
                                .whereEqualTo("Date", dateString)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        boolean shiftExists = false;
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String existingMorning = document.getString("Morning");
                                            String existingNoon = document.getString("Noon");
                                            String existingEvening = document.getString("Evening");

                                            if (existingMorning.equals(shiftData.get("Morning")) &&
                                                    existingNoon.equals(shiftData.get("Noon")) &&
                                                    existingEvening.equals(shiftData.get("Evening"))) {
                                                shiftExists = true;
                                                break;
                                            }
                                        }

                                        if (!shiftExists) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                db.collection("schedule").document(document.getId()).update(shiftData)
                                                        .addOnSuccessListener(aVoid -> Log.d("CREATE_SCHEDULE", "Shift updated for " + email + " on " + dateString))
                                                        .addOnFailureListener(e -> Log.w("CREATE_SCHEDULE", "Error updating shift", e));
                                            }
                                        } else {
                                            Log.d("CREATE_SCHEDULE", "Shift already exists for " + email + " on " + dateString + ". Skipping update.");
                                        }
                                    } else {
                                        db.collection("schedule").add(shiftData)
                                                .addOnSuccessListener(documentReference -> Log.d("CREATE_SCHEDULE", "Shift added with ID: " + documentReference.getId()))
                                                .addOnFailureListener(e -> Log.w("CREATE_SCHEDULE", "Error adding shift", e));
                                    }
                                });
                    }
                }
            }
        }

        // Show success popup
        showScheduleCreatedPopup();
    }
    private void showScheduleCreatedPopup() {
        // Inflate the popup layout
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_schedule_created, null);

        // Create the popup window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        // Get the Send to my email button and set the click listener
        Button sendEmailButton = popupView.findViewById(R.id.send_email_button);
        sendEmailButton.setOnClickListener(v -> {
            // Fetch the manager's email from the database
            String adminEmail = user.getEmail();
            if (adminEmail != null && !adminEmail.isEmpty()) {
                // Use the dynamically calculated startDate and endDate
                new GenerateExcelFileTask(adminEmail, startDate, endDate).execute();
            } else {
                showToast("Manager email not found.");
            }
            alertDialog.dismiss();

            // Navigate to the home page
            navigateToHomePage();
        });
    }

    private class GenerateExcelFileTask extends AsyncTask<Void, Void, Boolean> {
        private String adminEmail;
        private String startDate;
        private String endDate;
        private Exception exception;

        GenerateExcelFileTask(String adminEmail, String startDate, String endDate) {
            this.adminEmail = adminEmail;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                byte[] excelFile = generateExcelFile(startDate, endDate);
                sendEmailWithAttachment(adminEmail, "Weekly Schedule", "Please check the attached schedule.", excelFile);
                return true;
            } catch (IOException | ExecutionException | InterruptedException | ParseException e) {
                exception = e;
                Log.e("CREATE_SCHEDULE", "Failed to generate Excel file", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                showToast("Sent successfully");
            } else {
                showToast("Failed to generate schedule file.");
            }
        }
    }




    private void navigateToHomePage() {
        Intent intent = new Intent(this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }
    private byte[] generateExcelFile(String startDate, String endDate) throws IOException, ExecutionException, InterruptedException, ParseException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference scheduleRef = db.collection("schedule");

        // Query to get all shifts within the given date range
        Task<QuerySnapshot> future = scheduleRef.whereGreaterThanOrEqualTo("Date", startDate)
                .whereLessThanOrEqualTo("Date", endDate).get();
        List<DocumentSnapshot> documents = Tasks.await(future).getDocuments();

        // Data structure to store shifts
        Map<String, Map<String, List<String>>> shiftData = new HashMap<>();

        for (DocumentSnapshot document : documents) {
            String date = document.getString("Date");
            String email = document.getString("Email");
            String morning = document.getString("Morning");
            String noon = document.getString("Noon");
            String evening = document.getString("Evening");

            List<String> shifts = new ArrayList<>();
            if ("yes".equals(morning)) shifts.add("Morning");
            if ("yes".equals(noon)) shifts.add("Afternoon");
            if ("yes".equals(evening)) shifts.add("Evening");

            shiftData.computeIfAbsent(date, k -> new HashMap<>())
                    .computeIfAbsent(email, k -> new ArrayList<>())
                    .addAll(shifts);
        }

        // Create an Excel workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Schedule");

        // Set a larger font for the workbook
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create header row with employee emails
        Row headerRow = sheet.createRow(0);
        int colIdx = 1;
        for (String email : shiftData.values().iterator().next().keySet()) {
            Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(email);
            cell.setCellStyle(headerCellStyle);
        }

        // Create rows for each date
        int rowIdx = 1;
        for (String date : shiftData.keySet()) {
            Row row = sheet.createRow(rowIdx++);
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(date);

            Map<String, List<String>> employees = shiftData.get(date);
            colIdx = 1;
            for (String email : shiftData.values().iterator().next().keySet()) {
                Cell cell = row.createCell(colIdx++);
                List<String> shifts = employees.getOrDefault(email, Collections.emptyList());

                if (shifts.contains("Morning") && shifts.contains("Afternoon") && shifts.contains("Evening")) {
                    cell.setCellValue("All day");
                } else {
                    StringBuilder shiftText = new StringBuilder();
                    if (shifts.contains("Morning")) shiftText.append("Morning\n");
                    if (shifts.contains("Afternoon")) shiftText.append("Afternoon\n");
                    if (shifts.contains("Evening")) shiftText.append("Evening\n");
                    cell.setCellValue(shiftText.toString().trim());
                }
            }
        }

        // Set a default column width
        for (int i = 0; i < colIdx; i++) {
            sheet.setColumnWidth(i, 20 * 256); // 20 characters wide
        }

        // Write the output to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return bos.toByteArray();
    }

    private void sendEmailWithAttachment(String recipientEmail, String subject, String body, byte[] attachment) {
        new Thread(() -> {
            try {
                // Set up mail server properties
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Authenticate and create the mail session
                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication("hadarle2@ac.sce.ac.il", "oser gawu ghel btsv");
                    }
                });

                // Create the email message
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress("hadarle2@ac.sce.ac.il"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);

                // Create the message body part
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);

                // Create the attachment part
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new javax.activation.DataHandler(new javax.activation.DataSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(attachment);
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public String getContentType() {
                        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    }

                    @Override
                    public String getName() {
                        return "schedule.xlsx";
                    }
                }));
                attachmentPart.setFileName("schedule.xlsx");

                // Combine the message parts
                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(attachmentPart);
                message.setContent(multipart);

                // Send the email
                Transport.send(message);
                runOnUiThread(() -> showToast("Schedule email sent to " + recipientEmail));
            } catch (MessagingException e) {
                Log.e("EMAIL_SENDING", "Failed to send email", e);
                runOnUiThread(() -> showToast("Failed to send schedule email."));
            }
        }).start();
    }



}