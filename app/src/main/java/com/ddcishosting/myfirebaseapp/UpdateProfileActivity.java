package com.ddcishosting.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UpdateProfileActivity extends AppCompatActivity {
    private EditText fNameEditText, lNameEditText, deptEditText, enIDEditText, fullNameEditText;
    private String firstName, lastName, department, enrollmentID;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile");

        progressBar = findViewById(R.id.progress_bar_update_profile);
        fNameEditText = findViewById(R.id.edittext_first_name);
        lNameEditText = findViewById(R.id.edittext_last_name);
        deptEditText = findViewById(R.id.edittext_department);
        enIDEditText = findViewById(R.id.edittext_enrollment_ID);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // show profile data
        showProfile(firebaseUser);

        // update email
        Button buttonUpdateEmail = findViewById(R.id.update_profile_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });
        /*
        // update password
        Button buttonUpdatePassword = findViewById(R.id.update_profile_password);
        buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdatePasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });*/

        // update profile
        Button buttonUpdateProfile = findViewById(R.id.update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });



    }

    // update profile
    private void updateProfile(FirebaseUser firebaseUser) {
        String fNameText = fNameEditText.getText().toString();
        String deptText = deptEditText.getText().toString();
        String enIDText = enIDEditText.getText().toString();

        if (TextUtils.isEmpty(fNameText)) {
            Toast.makeText(UpdateProfileActivity.this, "First name cannot be empty!", Toast.LENGTH_SHORT).show();
            fNameEditText.setError("First name is required");
        } else if (TextUtils.isEmpty(deptText)) {
            Toast.makeText(UpdateProfileActivity.this, "Department name cannot be empty!", Toast.LENGTH_SHORT).show();
            lNameEditText.setError("Department name is required");
        } else if (TextUtils.isEmpty(enIDText)) {
            Toast.makeText(UpdateProfileActivity.this, "Enrollment ID cannot be empty!", Toast.LENGTH_SHORT).show();
            enIDEditText.setError("Enrollment ID is required");
        } else {
            progressBar.setVisibility(View.VISIBLE);

            // enter user data into the Firebase Realtime Database
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(deptText, enIDText);

            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // department name and enrollment id have been updated
                        // setting new display name
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(fNameText + " " + lNameEditText.getText()).build();
                        firebaseUser.updateProfile(profileUpdate);

                        Toast.makeText(UpdateProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(UpdateProfileActivity.this, "Refresh this page manually to see the changes", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);

                        // clear the stack to prevent the user from going back to UpdateProfileActivity on pressing back button after logging out
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // close onLoginClickedActivity
                    } else {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch(Exception e) {
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    // fetch data from Firebase and display
    private void showProfile(FirebaseUser firebaseUser) {
        String userID_of_registered = firebaseUser.getUid();

        // extracting user reference from database
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userID_of_registered).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);

                if (readUserDetails != null) {
                    String fullName = firebaseUser.getDisplayName();
                    department = readUserDetails.deptName;
                    enrollmentID = readUserDetails.enID;

                    String[] nameParts = fullName.split(" ");

                    // extracting the first name
                    firstName = nameParts[0];

                    // concatenating remaining parts to form the last name
                    lastName = "";
                    for (int i = 1; i < nameParts.length; i++) {
                        lastName += nameParts[i];

                        if (i < nameParts.length - 1) {
                            lastName += " ";
                        }
                    }

                    fNameEditText.setText(firstName);
                    lNameEditText.setText(lastName);
                    deptEditText.setText(department);
                    enIDEditText.setText(enrollmentID);
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
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

            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);

            // clear the stack to prevent the user from going back to onLoginClickedActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close onLoginClickedActivity
        }

        return super.onOptionsItemSelected(item);
    }
}