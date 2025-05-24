package com.example.communityapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EventDetailsActivity extends AppCompatActivity {

    ImageView eventImage;
    TextView eventTitle, eventDateTime, eventLocation, eventDescription, eventCategory;
    Button actionButton, deleteEventButton;
    ImageButton backButton;

    String eventId; // Add eventId to track the event

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventImage = findViewById(R.id.eventImage);
        eventTitle = findViewById(R.id.eventTitle);
        eventDateTime = findViewById(R.id.eventDate);
        eventLocation = findViewById(R.id.eventLocation);
        eventDescription = findViewById(R.id.eventDescription);
        eventCategory = findViewById(R.id.eventCategory);
        actionButton = findViewById(R.id.actionButton);
        backButton = findViewById(R.id.backButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);  // The new delete button

        backButton.setOnClickListener(v -> finish());

        // Get data from intent
        eventId = getIntent().getStringExtra("eventId"); // Pass eventId when launching this activity
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String location = getIntent().getStringExtra("location");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        int imageResId = getIntent().getIntExtra("image", R.drawable.beach_cleanup);
        String userRole = getIntent().getStringExtra("role");

        // Set UI elements
        eventTitle.setText(title != null ? title : "No Title");
        eventDateTime.setText(date != null ? date : "Date & Time TBD");
        eventLocation.setText(location != null ? "Location: " + location : "Location TBD");
        eventDescription.setText(description != null ? description : "No description available.");
        eventCategory.setText(category != null ? category : "Category Unknown");
        eventImage.setImageResource(imageResId);

        // Role-based button text and action
        if ("Organizer".equals(userRole)) {
            actionButton.setText("Manage Volunteers");
            actionButton.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Volunteer List...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ManageVolunteersActivity.class);
                intent.putExtra("eventId", eventId); // Pass event ID to manage volunteers
                startActivity(intent);
            });

            // Enable Delete Event button for Organizers
            deleteEventButton.setVisibility(View.VISIBLE); // Make it visible
            deleteEventButton.setOnClickListener(v -> {
                deleteEvent(eventId);  // Call the method to delete the event
            });
        } else {
            actionButton.setText("Join Event");
            actionButton.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(this, "Please log in to join events", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseRepository repo = new FirebaseRepository();
                repo.joinEvent(eventId, currentUser.getUid(),
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "You have joined the event!", Toast.LENGTH_SHORT).show();
                                // Optionally update UI, disable button, etc.
                            } else {
                                Toast.makeText(this, "Failed to join event", Toast.LENGTH_SHORT).show();
                            }
                        },
                        e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            });

            // Enable Delete Event button for Volunteers
            deleteEventButton.setVisibility(View.VISIBLE); // Make it visible
            deleteEventButton.setOnClickListener(v -> {
                removeVolunteerFromEvent(eventId);  // Call the method to remove the volunteer
            });
        }
    }

    /**
     * Delete event from Firestore (for organizers)
     * @param eventId The ID of the event to be deleted
     */
    private void deleteEvent(String eventId) {
        FirebaseRepository firebaseRepository = new FirebaseRepository();
        firebaseRepository.deleteEvent(eventId,
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EventDetailsActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to the previous screen after deletion
                    }
                },
                e -> Toast.makeText(EventDetailsActivity.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Remove the volunteer from the event (for volunteers)
     * @param eventId The ID of the event to remove the volunteer from
     */
    private void removeVolunteerFromEvent(String eventId) {
        FirebaseRepository firebaseRepository = new FirebaseRepository();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String volunteerId = currentUser.getUid();
            firebaseRepository.removeVolunteer(eventId, volunteerId,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EventDetailsActivity.this, "You have unjoined the event!", Toast.LENGTH_SHORT).show();
                                finish();  // Return to the previous screen after removal
                            } else {
                                Toast.makeText(EventDetailsActivity.this, "Failed to remove volunteer", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    e -> Toast.makeText(EventDetailsActivity.this, "Failed to remove volunteer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
