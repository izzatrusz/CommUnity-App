package com.example.communityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    RecyclerView eventRecyclerView;
    TabLayout categoryTabs;
    BottomNavigationView bottomNavigationView;

    FirebaseRepository firebaseRepository;

    EventAdapter adapter;
    ArrayList<Event> events = new ArrayList<>();
    ArrayList<Event> filteredEvents = new ArrayList<>();

    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseRepository = new FirebaseRepository();

        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        categoryTabs = findViewById(R.id.categoryTabs);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRole = getIntent().getStringExtra("role");

        adapter = new EventAdapter(this, filteredEvents, userRole);
        eventRecyclerView.setAdapter(adapter);

        loadAllEvents(); // Load all events initially

        setupCategoryTabs();

        setupBottomNavigation();
    }

    private void loadAllEvents() {
        firebaseRepository.getAllEvents(querySnapshot -> {
            if (querySnapshot == null || querySnapshot.isEmpty()) {
                Toast.makeText(DashboardActivity.this, "No events available.", Toast.LENGTH_SHORT).show();
                events.clear();
                filteredEvents.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            events.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String eventId = doc.getId();  // Get Firestore document ID
                String title = doc.getString("title");
                String date = doc.getString("date");
                String location = doc.getString("location");
                String description = doc.getString("description");
                String category = doc.getString("category");
                int imageResId = getImageForCategory(category); // Map category to image resource

                events.add(new Event(eventId, title, date, location, imageResId, category, description));
            }

            filterEventsByCategory("All"); // Show all events initially
        }, e -> Toast.makeText(DashboardActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupCategoryTabs() {
        categoryTabs.addTab(categoryTabs.newTab().setText("All"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Community"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Education"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Health"));

        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText() != null ? tab.getText().toString() : "All";
                filterEventsByCategory(selectedCategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void filterEventsByCategory(String category) {
        filteredEvents.clear();
        if (category.equals("All")) {
            filteredEvents.addAll(events);
        } else {
            for (Event event : events) {
                if (category.equalsIgnoreCase(event.getCategory())) {
                    filteredEvents.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private int getImageForCategory(String category) {
        if (category == null) return R.drawable.default_event_image; // fallback image

        switch (category.toLowerCase()) {
            case "environment": return R.drawable.environment_event;
            case "education": return R.drawable.education_event;
            case "health": return R.drawable.health_event;
            case "community": return R.drawable.community_event;
            default: return R.drawable.default_event_image;
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // Already on Dashboard
            } else if (id == R.id.nav_events) {
                Intent intent = new Intent(DashboardActivity.this, MyEventsActivity.class);
                intent.putExtra("role", userRole);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                intent.putExtra("role", userRole);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}
