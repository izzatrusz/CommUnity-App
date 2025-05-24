package com.example.communityapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    EditText editFullName, editUsername, editEmail, editPhone;
    Button saveProfileButton;
    ImageButton backButton;

    FirebaseRepository firebaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        editFullName = findViewById(R.id.editFullName);
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        backButton = findViewById(R.id.backButton);

        firebaseRepository = new FirebaseRepository();

        // Load user data from Firestore
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();  // Get the current user ID

        // Fetch user data from Firestore
        firebaseRepository.getUserProfile(userId, documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editFullName.setText(documentSnapshot.getString("fullName"));
                editUsername.setText(documentSnapshot.getString("username"));
                editEmail.setText(documentSnapshot.getString("email"));
                editPhone.setText(documentSnapshot.getString("phone"));
            } else {
                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }, e -> Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Save profile changes
        saveProfileButton.setOnClickListener(v -> {
            String fullName = editFullName.getText().toString().trim();
            String username = editUsername.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            // Validate input
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prepare data to update in Firestore
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("fullName", fullName);
            updatedData.put("username", username);
            updatedData.put("email", email);
            updatedData.put("phone", phone);

            // Call the method to update the user profile
            firebaseRepository.updateUserProfile(updatedData,
                    aVoid -> {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();  // Close the activity after updating
                    },
                    e -> Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Back button functionality
        backButton.setOnClickListener(v -> finish());
    }
}
