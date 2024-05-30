package com.example.schedule_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    public static final String TAG = "TAG";
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastName, editTextPhone;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textview;
    FirebaseFirestore fstore;
    String userID;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is already sign in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();//close the login and open the main activity
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
        progressBar=findViewById(R.id.progressBar);
        textview = findViewById(R.id.loginNow);
        fstore = FirebaseFirestore.getInstance();
        // this function says "if u click on the button that navigate to the login page then call the login class"
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);// navigate to the Login page
                startActivity(intent);
                finish();
            }
        });

        if(mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);// if the user already loged in
            startActivity(intent);
            finish();
        }




        //this function work when the user click on the button of "register now"
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, name, last_name, phone;
                email = editTextEmail.getText().toString().trim();
                password = editTextPassword.getText().toString();
                name = editTextName.getText().toString();
                last_name = editTextLastName.getText().toString();
                phone = String.valueOf(editTextPhone.getText());


                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(Registration.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(Registration.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length()<6){
                    editTextPassword.setError("Password must be 6 characters or more");
                }

                if(TextUtils.isEmpty(name)) {
                    Toast.makeText(Registration.this, "Enter name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(last_name)) {
                    Toast.makeText(Registration.this, "Enter last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(phone)) {
                    Toast.makeText(Registration.this, "Enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                // after the user clicked then the progressbar will be visible
                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // if task successful means if the user has successfully created
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registration.this, "Account has successfully created",
                                            Toast.LENGTH_SHORT).show();
                                    userID = mAuth.getCurrentUser().getUid();
                                    //creating collection called user and inside that we will have the other things
                                    DocumentReference documentReference = fstore.collection("users").document(userID);
                                    //creating Hase map  he made it as <string,object>
                                    Map<String, String> user = new HashMap<>();
                                    //and here now we are inserting our data as key and object
                                    user.put("First name", name);
                                    user.put("Last name", last_name);
                                    user.put("Phone", phone);
                                    user.put("Email", email);
                                    documentReference.set(user).addOnSuccessListener((OnSuccessListener)  (aVoid)-> {
                                                Log.d(TAG,"onSuccesses: user profile is created for "+ userID);
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.toString());
                                        }
                                    });
                                    //in this line he wrote "startActivity(new Intent(getApplicationContext(),MainActivity.class));" but i didn't and still os working

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Registration.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });


            }
        });
    }
}