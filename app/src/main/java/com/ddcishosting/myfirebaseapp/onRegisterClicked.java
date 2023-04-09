package com.ddcishosting.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class onRegisterClicked extends AppCompatActivity {

    private EditText regFName;
    private EditText regLName;
    private EditText regDepartment;
    private EditText regEnrollID;
    private EditText regEmail;
    private EditText regPassword;
    private ProgressBar progressBar;

    private static final String TAG = "onRegisterClicked";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_register_clicked);

        getSupportActionBar().setTitle("Register");

        Toast.makeText(this, "You can register now...", Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progress_bar);
        regFName = findViewById(R.id.firstName);
        regLName = findViewById(R.id.lastName);
        regDepartment = findViewById(R.id.departmentName);
        regEnrollID = findViewById(R.id.enrollmentID);
        regEmail = findViewById(R.id.email);
        regPassword = findViewById(R.id.password);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // obtaining the entered details into String variables
                String fNameText = regFName.getText().toString();
                String lNameText = regLName.getText().toString();
                String deptText = regDepartment.getText().toString();
                String enIDText = regEnrollID.getText().toString();
                String pwdText = regPassword.getText().toString();
                String emailText = regEmail.getText().toString();

                if (TextUtils.isEmpty(fNameText)) {
                    Toast.makeText(onRegisterClicked.this, "First name cannot be empty!", Toast.LENGTH_SHORT).show();
                    regFName.setError("First name is required");
                } else if (TextUtils.isEmpty(deptText)) {
                    Toast.makeText(onRegisterClicked.this, "Department name cannot be empty!", Toast.LENGTH_SHORT).show();
                    regDepartment.setError("Department name is required");
                } else if (TextUtils.isEmpty(enIDText)) {
                    Toast.makeText(onRegisterClicked.this, "Enrollment ID cannot be empty!", Toast.LENGTH_SHORT).show();
                    regEnrollID.setError("Enrollment ID is required");
                } else if (TextUtils.isEmpty(pwdText)) {
                    Toast.makeText(onRegisterClicked.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                    regPassword.setError("Password is required");
                } else if (TextUtils.isEmpty(emailText)) {
                    Toast.makeText(onRegisterClicked.this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
                    regPassword.setError("Email is required");
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(fNameText, lNameText, deptText, enIDText, emailText, pwdText);
                }
            }
        });
    }

    // register user using the credentials
    private void registerUser(String fNameText, String lNameText, String deptText, String enIDText, String emailText, String pwdText) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(emailText, pwdText).addOnCompleteListener(onRegisterClicked.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // update Display Name of User
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(fNameText + " " + lNameText).build();
                    assert firebaseUser != null;
                    firebaseUser.updateProfile(profileChangeRequest);

                    // enter user data into Firebase Realtime Database
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(deptText, enIDText);

                    // extracting user reference from database for 'Registered Users'
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(onRegisterClicked.this, "User registered successfully.", Toast.LENGTH_LONG).show();

//                              // open user profile after successful registration
                                Intent intent = new Intent(onRegisterClicked.this, onLoginClicked.class);
                                // to prevent user from returning back to Register Activity on pressing back button after registration
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); // to close Register Activity
                            } else {
                                Toast.makeText(onRegisterClicked.this, "User registered failed. Please try again.", Toast.LENGTH_LONG).show();
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });

                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        regEmail.setError("Your email is either invalid or already exists. Kindly re-check.");
                        regEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        regEmail.setError("Your email is already in use. Kindly login or use another email.");
                        regEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(onRegisterClicked.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}