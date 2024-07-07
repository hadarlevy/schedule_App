package com.example.schedule_application;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SupportActivity extends NavBarActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;

    EditText nameInput;
    EditText emailInput;
    EditText phoneInput;
    EditText descriptionInput;
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        descriptionInput = findViewById(R.id.description_input);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString();
                String email = emailInput.getText().toString();
                String phone = phoneInput.getText().toString();
                String description = descriptionInput.getText().toString();

                if (name.isEmpty() || email.isEmpty() || description.isEmpty()) {
                    Toast.makeText(SupportActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                } else {
                    sendEmail(name, email, phone, description);
                }
            }
        });
    }

    private void sendEmail(final String name, final String email, final String phone, final String description) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties properties = new Properties();
                    properties.put("mail.smtp.host", "smtp.gmail.com");
                    properties.put("mail.smtp.port", "587");
                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.smtp.starttls.enable", "true");

                    Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new javax.mail.PasswordAuthentication("hadarle2@ac.sce.ac.il", "oser gawu ghel btsv");
                        }
                    });

                    Message message = new MimeMessage(session);
                    // Set the From address dynamically based on user input
                    String senderEmail = emailInput.getText().toString().trim(); // Get user's email input
                    InternetAddress fromAddress = new InternetAddress(senderEmail);
                    message.setFrom(fromAddress);
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("hadarle2@ac.sce.ac.il"));
                    message.setSubject("Support Request from " + name);
                    message.setText("Name: " + name + "\nEmail: " + email + "\nPhone: " + phone + "\n\nDescription:\n" + description);

                    Transport.send(message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlertDialog("Email Sent", "You will receive a response at " + email + " in a few days.");
                            // Optional: clear input fields
                            nameInput.setText("");
                            emailInput.setText("");
                            phoneInput.setText("");
                            descriptionInput.setText("");
                        }
                    });

                } catch (MessagingException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlertDialog("Failed to Send Email", "Please try again later.");
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
