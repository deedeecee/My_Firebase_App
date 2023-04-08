package com.ddcishosting.myfirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class onLoginClicked extends AppCompatActivity {
    private TextView welcomeMessage, fullName, department, enrollmentID, emailID;
    private ProgressBar progressBar;
    private String fullNameString, departmentString, enrollmentIDString, emailIDString;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_login_clicked);

        getSupportActionBar().setTitle("Dashboard");

        welcomeMessage = findViewById(R.id.show_welcome_message);
        fullName = findViewById(R.id.show_full_name);
        department = findViewById(R.id.show_department);
        enrollmentID = findViewById(R.id.show_enrollment_ID);
        emailID = findViewById(R.id.show_email_ID);
        progressBar = findViewById(R.id.progress_bar_dashboard);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(onLoginClicked.this, "Something went wrong!\nUser's details are unavailable", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();

        // extracting user reference from database for "Registered Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);

                if (readUserDetails != null) {
                    fullNameString = firebaseUser.getDisplayName();
                    emailIDString = firebaseUser.getEmail();
                    departmentString = readUserDetails.deptName;
                    enrollmentIDString = readUserDetails.enID;

                    welcomeMessage.setText("Welcome, " + fullNameString);
                    fullName.setText(fullNameString);
                    department.setText(departmentString);
                    enrollmentID.setText(enrollmentIDString);
                    emailID.setText(emailIDString);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(onLoginClicked.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(onLoginClicked.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }/* else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(onLoginClicked.this, UpdateEmailActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_password) {
            Intent intent = new Intent(onLoginClicked.this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(onLoginClicked.this, DeleteProfileActivity.class);
            startActivity(intent);
        }*/ else if (id == R.id.menu_log_out) {
            authProfile.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(onLoginClicked.this, MainActivity.class);

            // clear the stack to prevent the user from going back to onLoginClickedActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close onLoginClickedActivity
        }

        return super.onOptionsItemSelected(item);
    }
}