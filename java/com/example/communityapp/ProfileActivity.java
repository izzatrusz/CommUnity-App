package com.example.communityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;

public class ProfileActivity extends AppCompatActivity {

    TextView userName, userUsername, userEmail, userPhone, userRole, userSummary;
    Button editProfileButton, settingsButton, changePasswordButton, logoutButton;
    BottomNavigationView bottomNavigationView;

    FirebaseRepository firebaseRepository;
    FirebaseUser currentUser;

    String role; // user role loaded from Firestore or intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseRepository = new FirebaseRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize Views
        userName = findViewById(R.id.userName);
        userUsername = findViewById(R.id.userUsername);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);
        userRole = findViewById(R.id.userRole);
        userSummary = findViewById(R.id.userSummary); // This TextView will be updated dynamically
        editProfileButton = findViewById(R.id.editProfileButton);
        settingsButton = findViewById(R.id.settingsButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Check if the user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get role from Firestore or Intent
        role = getIntent().getStringExtra("role");

        // Load user profile data from Firestore
        firebaseRepository.getUserProfile(currentUser.getUid(), new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Set text fields based on Firestore data
                    userName.setText(documentSnapshot.getString("fullName"));
                    userUsername.setText(documentSnapshot.getString("username"));
                    userEmail.setText(documentSnapshot.getString("email"));
                    userPhone.setText(documentSnapshot.getString("phone"));
                    role = documentSnapshot.getString("role");
                    userRole.setText(role != null ? role : "Unknown");

                    // Set the summary based on the role
                    if ("Organizer".equalsIgnoreCase(role)) {
                        userSummary.setText("Total Events Created: ");
                        loadOrganizerEventCount(currentUser.getUid());  // Fetch events created by the organizer
                    } else {
                        userSummary.setText("Badges Earned: ");
                        loadVolunteerBadgeCount(currentUser.getUid());  // Fetch events joined by the volunteer
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }
        }, e -> Toast.makeText(ProfileActivity.this, "Error loading data. Please try again later.", Toast.LENGTH_SHORT).show());

        // Edit Profile Button
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Settings Button
        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        // Change Password Button
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Bottom Navigation Setup
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent;
                if ("Organizer".equalsIgnoreCase(role)) {
                    intent = new Intent(this, OrganizerDashboardActivity.class);
                } else {
                    intent = new Intent(this, DashboardActivity.class);
                }
                intent.putExtra("role", role);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_events) {
                Intent intent = new Intent(this, MyEventsActivity.class);
                intent.putExtra("role", role);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                return true; // Already on this page
            }
            return false;
        });
    }

    // Method to load the number of events created by the organizer
    private void loadOrganizerEventCount(String organizerId) {
        firebaseRepository.getEventsCreatedByUser(organizerId,
                querySnapshot -> {
                    int totalEventsCount = querySnapshot.size();
                    userSummary.setText("Total Events Created: " + totalEventsCount);
                },
                e -> Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to load the number of events joined by the volunteer
    private void loadVolunteerBadgeCount(String volunteerId) {
        firebaseRepository.getEventsJoinedByUser(volunteerId,
                querySnapshot -> {
                    int totalBadgesCount = querySnapshot.size();
                    userSummary.setText("Badges Earned: " + totalBadgesCount);
                },
                e -> Toast.makeText(this, "Failed to load badges: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
