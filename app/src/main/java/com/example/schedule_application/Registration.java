package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Registration extends AppCompatActivity {
    public static final String TAG = "RegistrationActivity";
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastName, editTextPhone;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textview;
    FirebaseFirestore fstore;
    String userID;
    boolean isEmailUnique = false;
    boolean isPhoneUnique = false;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); // Close the login and open the main activity
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextName = findViewById(R.id.name);
        editTextLastName = findViewById(R.id.lastname);
        editTextPhone = findViewById(R.id.phone);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textview = findViewById(R.id.loginNow);
        fstore = FirebaseFirestore.getInstance();

        // Navigate to the Login page when "loginNow" is clicked
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Add text watchers for real-time validation
        addTextWatchers();

        // Registration button click listener
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Register button clicked");
                progressBar.setVisibility(View.VISIBLE);
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString();
                String name = editTextName.getText().toString();
                String last_name = editTextLastName.getText().toString();
                String phone = editTextPhone.getText().toString();

                if (isInputValid(email, password, name, last_name, phone)){
                    checkIfPhoneExists(phone);
                    checkIfEmailExists(email);
                    checkEmailAndPhoneAndRegister(email, password, name, last_name, phone);

                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void addTextWatchers() {
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
                checkIfEmailExists(editTextEmail.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone();
                checkIfPhoneExists(editTextPhone.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateEmail() {
        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Enter email");
        } else if (!isValidEmail(email)) {
            editTextEmail.setError("Enter a valid email address");
        } else {
            editTextEmail.setError(null);
        }
    }

    private void validatePassword() {
        String password = editTextPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Enter password");
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be 6 characters or more");
        } else {
            editTextPassword.setError(null);
        }
    }

    private void validateName() {
        String name = editTextName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Enter name");
        } else if (name.length() > 20 || !name.matches("[a-zA-Z]+")) {
            editTextName.setError("Name must be in English, without spaces, and less than 20 characters");
        } else {
            editTextName.setError(null);
        }
    }

    private void validateLastName() {
        String last_name = editTextLastName.getText().toString();
        if (TextUtils.isEmpty(last_name)) {
            editTextLastName.setError("Enter last name");
        } else if (last_name.length() > 20 || !last_name.matches("[a-zA-Z]+")) {
            editTextLastName.setError("Last name must be in English, without spaces, and less than 20 characters");
        } else {
            editTextLastName.setError(null);
        }
    }

    private void validatePhone() {
        String phone = editTextPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Enter phone number");
        } else if (!phone.matches("^(\\+972|0)([23489]|5[0248]|7[3678])\\d{7}$")) {
            editTextPhone.setError("Enter a valid Israeli phone number");
        } else {
            editTextPhone.setError(null);
        }
    }
    private boolean isInputValid(String email, String password, String name, String last_name, String phone) {
        validateEmail();
        validatePhone();
        validatePassword();
        validateName();
        validateLastName();

        boolean isValid = editTextEmail.getError() == null &&
                editTextPassword.getError() == null &&
                editTextName.getError() == null &&
                editTextLastName.getError() == null &&
                editTextPhone.getError() == null;

        Log.d(TAG, "Input validation result: " + isValid);
        return isValid;
    }

    private void checkIfEmailExists(String email) {
        fstore.collection("users")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            editTextEmail.setError("Email is already registered");
                            isEmailUnique = false;
                        } else {
                            isEmailUnique = true;
                            if (isEmailUnique && isPhoneUnique) {
                                buttonReg.setEnabled(true);
                            } else {
                                buttonReg.setEnabled(false);
                            }
                        }
                    }
                });
    }

    private void checkIfPhoneExists(String phone) {
        fstore.collection("users")
                .whereEqualTo("Phone", phone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            editTextPhone.setError("Phone number is already registered");
                            isPhoneUnique = false;
                        } else {
                            isPhoneUnique = true;
                            if (isEmailUnique && isPhoneUnique) {
                                buttonReg.setEnabled(true);
                            } else {
                                buttonReg.setEnabled(false);
                            }
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }

        // List of allowed email domains
        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add("gmail.com");
        allowedDomains.add("hotmail.com");
        allowedDomains.add("ac.sce.ac.il");
        // Add more allowed domains as needed

        String domain = email.substring(email.indexOf('@') + 1);
        return allowedDomains.contains(domain);
    }

    private void checkEmailAndPhoneAndRegister(String email, String password, String name, String last_name, String phone) {
        if (isEmailUnique && isPhoneUnique) {
            registerUser(email, password, name, last_name, phone);
        } else {
            Toast.makeText(this, "Please ensure email and phone number are unique", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void registerUser(String email, String password, String name, String last_name, String phone) {
        Log.d(TAG, "registerUser function called");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Verify email address (send verification link)
                            FirebaseUser fuser = mAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(Registration.this, "Verification Email Has been Sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "OnFailure: Email not sent " + e.getMessage());
                                }
                            });

                            Toast.makeText(Registration.this, "Account has been successfully created",
                                    Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();
                            // Create user document in Firestore
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, String> user = new HashMap<>();
                            user.put("First name", name);
                            user.put("Last name", last_name);
                            user.put("Phone", phone);
                            user.put("Email", email);
                            documentReference.set(user).addOnSuccessListener((OnSuccessListener<Void>) (aVoid) -> {
                                Log.d(TAG, "onSuccess: user profile is created for " + userID);
                                progressBar.setVisibility(View.GONE); // Hide progress bar after successful registration
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                    progressBar.setVisibility(View.GONE); // Hide progress bar if Firestore write fails
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "Authentication failed: " + task.getException());
                            Toast.makeText(Registration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide progress bar if authentication fails
                        }
                    }
                });
    }
}