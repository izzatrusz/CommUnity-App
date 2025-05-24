package com.example.communityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MyEventsActivity extends AppCompatActivity {

    RecyclerView myEventsRecyclerView;
    BottomNavigationView bottomNavigationView;

    FirebaseRepository firebaseRepository;
    FirebaseUser currentUser;

    EventAdapter adapter;
    ArrayList<Event> events = new ArrayList<>();

    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        firebaseRepository = new FirebaseRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        myEventsRecyclerView = findViewById(R.id.myEventsRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        myEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRole = getIntent().getStringExtra("role");

        adapter = new EventAdapter(this, events, userRole);
        myEventsRecyclerView.setAdapter(adapter);

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Organizer".equalsIgnoreCase(userRole)) {
            loadEventsCreatedByUser(currentUser.getUid());
        } else {
            loadEventsJoinedByUser(currentUser.getUid());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent;
                if ("Organizer".equalsIgnoreCase(userRole)) {
                    intent = new Intent(this, OrganizerDashboardActivity.class);
                } else {
                    intent = new Intent(this, DashboardActivity.class);
                }
                intent.putExtra("role", userRole);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_events) {
                return true; // Already on this page
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("role", userRole);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void loadEventsCreatedByUser(String organizerId) {
        firebaseRepository.getEventsCreatedByUser(organizerId,
                querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(MyEventsActivity.this, "No Events Created Yet", Toast.LENGTH_SHORT).show();
                        events.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    events.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String eventId = doc.getId();  // Get Firestore document ID
                        String title = doc.getString("title");
                        String date = doc.getString("date");
                        String location = doc.getString("location");
                        String description = doc.getString("description");
                        String category = doc.getString("category");
                        int imageResId = getImageForCategory(category); // Map category to image resource

                        events.add(new Event(eventId, title, date, location, imageResId, category, description));
                    }
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(MyEventsActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadEventsJoinedByUser(String userId) {
        firebaseRepository.getEventsJoinedByUser(userId,
                querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(MyEventsActivity.this, "No Joined Events Yet", Toast.LENGTH_SHORT).show();
                        events.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    events.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String eventId = doc.getId();  // Get Firestore document ID
                        String title = doc.getString("title");
                        String date = doc.getString("date");
                        String location = doc.getString("location");
                        String description = doc.getString("description");
                        String category = doc.getString("category");
                        int imageResId = getImageForCategory(category); // Map category to image resource

                        events.add(new Event(eventId, title, date, location, imageResId, category, description));
                    }
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(MyEventsActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int getImageForCategory(String category) {
        if (category == null) return R.drawable.default_event_image;

        switch (category.toLowerCase()) {
            case "environment":
                return R.drawable.environment_event;
            case "education":
                return R.drawable.education_event;
            case "health":
                return R.drawable.health_event;
            case "community":
                return R.drawable.community_event;
            default:
                return R.drawable.default_event_image;
        }
    }
}
