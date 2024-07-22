package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastName, editTextPhone, editTextAdminCode;
    Button buttonReg;
    CheckBox termsCheckBox;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textview, adminCodeLabel;
    FirebaseFirestore fStore;
    String userID;
    boolean isEmailUnique = false;
    boolean isPhoneUnique = false;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
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
        editTextAdminCode = findViewById(R.id.adminCode);
        buttonReg = findViewById(R.id.btn_register);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        progressBar = findViewById(R.id.progressBar);
        textview = findViewById(R.id.loginNow);
        adminCodeLabel = findViewById(R.id.adminCodeLabel);
        fStore = FirebaseFirestore.getInstance();

        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        addTextWatchers();

        termsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    adminCodeLabel.setVisibility(View.VISIBLE);
                    editTextAdminCode.setVisibility(View.VISIBLE);
                } else {
                    adminCodeLabel.setVisibility(View.GONE);
                    editTextAdminCode.setVisibility(View.GONE);
                }
            }
        });

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

                if (isInputValid(email, password, name, last_name, phone)) {
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

    private void checkIfEmailExists(String email) {
        fStore.collection("users")
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
        fStore.collection("users")
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

    private void checkEmailAndPhoneAndRegister(String email, String password, String name, String last_name, String phone) {
        if (isEmailUnique && isPhoneUnique) {
            if (termsCheckBox.isChecked()) {
                String adminCode = editTextAdminCode.getText().toString().trim();
                if ("1111".equals(adminCode)) {
                    registerAdmin(email, password, name, last_name, phone);
                } else {
                    Toast.makeText(Registration.this, "Incorrect admin code", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                }
            } else {
                registerUser(email, password, name, last_name, phone);
            }
        } else {
            Toast.makeText(this, "Please ensure email and phone number are unique", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void registerAdmin(String email, String password, String name, String last_name, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                userID = user.getUid();
                                DocumentReference documentReference = fStore.collection("Manager").document(userID);
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("First name", name);
                                userMap.put("Last name", last_name);
                                userMap.put("Phone", phone);
                                userMap.put("Email", email);
                                userMap.put("UserID", userID);
                                documentReference.set(userMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Manager profile created successfully");
                                                Intent intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
                                                startActivity(intent);
                                                finish(); // Close the registration activity
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error creating manager profile", e);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Registration.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registerUser(String email, String password, String name, String last_name, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                userID = user.getUid();
                                DocumentReference documentReference = fStore.collection("users").document(userID);
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("First name", name);
                                userMap.put("Last name", last_name);
                                userMap.put("Phone", phone);
                                userMap.put("Email", email);
                                userMap.put("UserID", userID);
                                documentReference.set(userMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "User profile created successfully");
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish(); // Close the registration activity
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error creating user profile", e);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Registration.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
