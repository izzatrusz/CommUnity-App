package com.example.communityapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private final ArrayList<Event> eventList;
    private final String userRole;

    public EventAdapter(Context context, ArrayList<Event> eventList, String userRole) {
        this.context = context;
        this.eventList = eventList;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());
        holder.eventLocation.setText(event.getLocation());
        holder.eventImage.setImageResource(event.getImage());
        //holder.eventDescription.setText(event.getDescription());

        if ("Organizer".equals(userRole)) {
            holder.joinButton.setText("Manage Volunteers");
        } else {
            holder.joinButton.setText("Join Event");
        }

        holder.joinButton.setOnClickListener(v -> {
            if ("Organizer".equals(userRole)) {
                Toast.makeText(context, "Managing: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ManageVolunteersActivity.class);
                intent.putExtra("eventId", event.getId()); // Pass eventId here
                context.startActivity(intent);
            } else {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(context, "Please log in to join events", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseRepository repo = new FirebaseRepository();
                repo.joinEvent(event.getId(), currentUser.getUid(),
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Joined: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to join event", Toast.LENGTH_SHORT).show();
                            }
                        },
                        e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getId());             // Pass eventId
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("location", event.getLocation());
            intent.putExtra("image", event.getImage());
            intent.putExtra("category", event.getCategory());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("role", userRole);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDate, eventLocation, eventDescription;
        ImageView eventImage;
        Button joinButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            //eventDescription = itemView.findViewById(R.id.eventDescription);
            eventImage = itemView.findViewById(R.id.eventImage);
            joinButton = itemView.findViewById(R.id.joinButton);
        }
    }
}
