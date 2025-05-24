package com.example.communityapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ManageVolunteersActivity extends AppCompatActivity {

    RecyclerView volunteersRecyclerView;
    ImageButton backButton;

    FirebaseRepository firebaseRepository;
    String eventId;
    ArrayList<Volunteer> volunteers = new ArrayList<>();
    VolunteerAdapter volunteerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_volunteers);

        firebaseRepository = new FirebaseRepository();

        // Get eventId from the intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton = findViewById(R.id.backButton);
        volunteersRecyclerView = findViewById(R.id.volunteersRecyclerView);
        volunteersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the adapter for RecyclerView
        volunteerAdapter = new VolunteerAdapter(this, volunteers, eventId);
        volunteersRecyclerView.setAdapter(volunteerAdapter);

        // Back button listener to navigate to the previous screen
        backButton.setOnClickListener(v -> finish());

        // Load volunteers for the event
        loadVolunteersForEvent(eventId);
    }

    private void loadVolunteersForEvent(String eventId) {
        firebaseRepository.getVolunteersForEvent(eventId,
                documents -> {
                    volunteers.clear();  // Clear the list before adding new data
                    for (DocumentSnapshot doc : documents) {
                        // Cast the DocumentSnapshot to QueryDocumentSnapshot
                        QueryDocumentSnapshot queryDoc = (QueryDocumentSnapshot) doc;

                        // Extract volunteer details from Firestore
                        String id = queryDoc.getId();
                        String fullName = queryDoc.getString("fullName");
                        String username = queryDoc.getString("username");
                        String phone = queryDoc.getString("phone");

                        // Add volunteer to the list
                        volunteers.add(new Volunteer(id, fullName, username, phone));
                    }
                    volunteerAdapter.notifyDataSetChanged();  // Notify adapter that data is updated
                },
                e -> Toast.makeText(this, "Failed to load volunteers: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
