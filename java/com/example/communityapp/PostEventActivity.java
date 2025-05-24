package com.example.communityapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class PostEventActivity extends AppCompatActivity {

    EditText eventTitleInput, eventDateInput, eventLocationInput, eventDescriptionInput;
    Spinner eventCategorySpinner;
    Button createEventButton;
    ImageButton backButton;

    FirebaseRepository firebaseRepository;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_event);

        firebaseRepository = new FirebaseRepository();
        mAuth = FirebaseAuth.getInstance();

        eventTitleInput = findViewById(R.id.eventTitleInput);
        eventDateInput = findViewById(R.id.eventDateInput);
        eventLocationInput = findViewById(R.id.eventLocationInput);
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput);
        eventCategorySpinner = findViewById(R.id.eventCategorySpinner);
        createEventButton = findViewById(R.id.createEventButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish()); // Back action

        String[] categories = {"Environment", "Education", "Health", "Community"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        eventCategorySpinner.setAdapter(adapter);

        createEventButton.setOnClickListener(v -> {
            String title = eventTitleInput.getText().toString().trim();
            String date = eventDateInput.getText().toString().trim();
            String location = eventLocationInput.getText().toString().trim();
            String description = eventDescriptionInput.getText().toString().trim();
            String category = eventCategorySpinner.getSelectedItem().toString();

            if (title.isEmpty() || date.isEmpty() || location.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prepare event data
            String organizerId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : null;
            if (organizerId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("title", title);
            eventData.put("date", date);
            eventData.put("location", location);
            eventData.put("description", description);
            eventData.put("category", category);
            eventData.put("organizerId", organizerId);
            eventData.put("volunteers", new java.util.ArrayList<String>()); // empty volunteer list initially
            eventData.put("volunteersAttended", new java.util.ArrayList<String>()); // empty attendance list initially

            // Create event in Firestore
            firebaseRepository.createEvent(eventData,
                    documentReference -> {
                        Toast.makeText(this, "Event Created Successfully", Toast.LENGTH_SHORT).show();
                        finish(); // close activity after successful creation
                    },
                    e -> Toast.makeText(this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
}
