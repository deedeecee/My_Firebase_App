package com.ddcishosting.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class UpdateEmailActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated, textViewCurrentEmail;
    private String userPreviousEmail, userNewEmail, userPassword;
    private Button buttonUpdateEmail, buttonVerifyUser;
    private EditText editTextNewEmail, editTextPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");

        progressBar = findViewById(R.id.progress_bar_update_email);
        textViewCurrentEmail = findViewById(R.id.current_email_value);
        textViewAuthenticated = findViewById(R.id.password_verification_text);
        editTextPassword = findViewById(R.id.edittext_password);
        editTextNewEmail = findViewById(R.id.edittext_new_email);
        buttonUpdateEmail = findViewById(R.id.update_email_button);
        buttonVerifyUser = findViewById(R.id.button_authenticate);

        buttonUpdateEmail.setEnabled(false);    // button is disabled until user verifies using password
        editTextNewEmail.setEnabled(false);     // new email is disabled until user verifies using password

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        // set old email id into textview
        if (firebaseUser.equals("")) {
            Toast.makeText(this, "Something went wrong!\nUser details are unavailable.", Toast.LENGTH_SHORT).show();
        } else {
            userPreviousEmail = firebaseUser.getEmail();
            textViewCurrentEmail.setText(userPreviousEmail);
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        buttonVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPassword = editTextPassword.getText().toString();   // obtain password for verification

                if (TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(UpdateEmailActivity.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Please enter password for verification!");
                    editTextPassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(userPreviousEmail, userPassword); // takes two parameters to authenticate

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("ResourceAsColor")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified.\nYou can update your email now!", Toast.LENGTH_LONG).show();

                                // set textview text informing the user about verification
                                textViewAuthenticated.setText("Password has been verified!");

                                editTextPassword.setEnabled(false); // no need to re-enter password
                                // user can now update email
                                editTextNewEmail.setEnabled(true);
                                buttonUpdateEmail.setEnabled(true);

                                // change color of update email button
                                buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this, R.color.white));

                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();

                                        if (TextUtils.isEmpty(userNewEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("New email cannot be empty!");
                                            editTextNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            Toast.makeText(UpdateEmailActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Enter a valid email address!");
                                            editTextNewEmail.requestFocus();
                                        } else if (userNewEmail.matches(userPreviousEmail)) {
                                            Toast.makeText(UpdateEmailActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("New email cannot be the same as previous email!");
                                            editTextNewEmail.requestFocus();
                                        } else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });

                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // verify user -> to be done later
//                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmailActivity.this, "Email has been updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // creating ActionBar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // when any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            // refresh the activity
            startActivity(getIntent());
            finish();

            // restricting any animation while refreshing
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateEmailActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
        }/* else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(onLoginClicked.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(onLoginClicked.this, DeleteProfileActivity.class);
            startActivity(intent);
        }*/ else if (id == R.id.menu_log_out) {
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);

            // clear the stack to prevent the user from going back to onLoginClickedActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close onLoginClickedActivity
        }

        return super.onOptionsItemSelected(item);
    }
}