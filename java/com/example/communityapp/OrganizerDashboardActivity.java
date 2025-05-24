package com.example.communityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class OrganizerDashboardActivity extends AppCompatActivity {

    Button postEventButton;
    TextView totalEvents, totalVolunteers;
    BottomNavigationView bottomNavigationView;

    FirebaseRepository firebaseRepository;
    FirebaseUser currentUser;

    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        firebaseRepository = new FirebaseRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userRole = getIntent().getStringExtra("role");

        postEventButton = findViewById(R.id.postEventButton);
        totalEvents = findViewById(R.id.totalEvents);
        totalVolunteers = findViewById(R.id.totalVolunteers);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        loadEventAndVolunteerCounts(currentUser.getUid());

        postEventButton.setOnClickListener(v -> {
            startActivity(new Intent(this, PostEventActivity.class));
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // Already here
            } else if (id == R.id.nav_events) {
                Intent intent = new Intent(this, MyEventsActivity.class);
                intent.putExtra("role", "Organizer");
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("role", "Organizer");
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadEventAndVolunteerCounts(String organizerId) {
        firebaseRepository.getEventsCreatedByUser(organizerId,
                querySnapshot -> {
                    int totalVolunteersCount = 0;
                    int totalEventsCount = 0;

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        totalEventsCount = querySnapshot.size();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            // Count volunteers array size
                            Object volunteersObj = doc.get("volunteers");
                            if (volunteersObj instanceof java.util.List) {
                                totalVolunteersCount += ((java.util.List<?>) volunteersObj).size();
                            }
                        }
                    }

                    totalEvents.setText("Total Events\n" + totalEventsCount);
                    totalVolunteers.setText("Total Volunteers\n" + totalVolunteersCount);

                },
                e -> Toast.makeText(this, "Failed to load counts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
