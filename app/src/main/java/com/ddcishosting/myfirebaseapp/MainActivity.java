package com.ddcishosting.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText enID, pwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "MainActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setting the title
        Objects.requireNonNull(getSupportActionBar()).setTitle("Firebase App");

        enID = findViewById(R.id.usernameLogin);
        pwd = findViewById(R.id.passwordLogin);
        progressBar = findViewById(R.id.progress_bar_login);

        // Initializing FirebaseAuth instance
        authProfile = FirebaseAuth.getInstance();

        ImageView imageViewShowHidePwd = findViewById(R.id.imageViewShowHidePwd);
        imageViewShowHidePwd.setImageResource(R.drawable.hidden);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectionStart = pwd.getSelectionStart();
                int selectionEnd = pwd.getSelectionEnd();
                if (pwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // if password is visible then hide it
                    pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    // change icon to visible
                    imageViewShowHidePwd.setImageResource(R.drawable.hidden);
                } else {
                    pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.view);
                }

                pwd.setSelection(selectionStart, selectionEnd);
            }
        });
        // open login activity
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enIDText = enID.getText().toString();
                String pwdText = pwd.getText().toString();

                if (TextUtils.isEmpty(enIDText) || TextUtils.isEmpty(pwdText)) {
                    Toast.makeText(MainActivity.this, "All fields are mandatory", Toast.LENGTH_LONG).show();
                    enID.requestFocus();
                    pwd.requestFocus();
                } else {
                    loginUser(enIDText, pwdText);
                }

                progressBar.setVisibility(View.VISIBLE);
            }
        });

        // open register activity
        TextView registerText = findViewById(R.id.registerClick);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, onRegisterClicked.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String enIDText, String pwdText) {
        authProfile.signInWithEmailAndPassword(enIDText.substring(0, 3).toLowerCase() + enIDText.substring(3) + "@tezu.ac.in", pwdText).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    if (firebaseUser != null) {
                        Toast.makeText(MainActivity.this, "You are logged in now", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(MainActivity.this, onLoginClicked.class));
                        finish();
                    }
                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthInvalidUserException e) {
                        enID.setError("User doesn't exist. Please register again.");
                        enID.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        pwd.setError("Invalid credentials. Kindly, check and re-enter.");
                        pwd.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // check if user is already logged in. If yes, then straightaway go to user profile
    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            Toast.makeText(MainActivity.this, "Already logged in...", Toast.LENGTH_SHORT).show();

            // start UserProfileActivity
            startActivity(new Intent(MainActivity.this, onLoginClicked.class));
            finish();
        } else {
            Toast.makeText(this, "You can log in now...", Toast.LENGTH_SHORT).show();
        }
    }
}