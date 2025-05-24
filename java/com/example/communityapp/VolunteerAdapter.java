package com.example.communityapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    private final Context context;
    private final ArrayList<Volunteer> volunteers;
    private final String eventId; // For reference to event where volunteer belongs

    FirebaseFirestore db;

    public VolunteerAdapter(Context context, ArrayList<Volunteer> volunteers, String eventId) {
        this.context = context;
        this.volunteers = volunteers;
        this.eventId = eventId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.volunteer_card, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Volunteer volunteer = volunteers.get(position);
        holder.volunteerName.setText(volunteer.getFullName());
        holder.volunteerPhone.setText(volunteer.getPhone());

        holder.attendedButton.setOnClickListener(v -> {
            // Mark volunteer as attended for this event
            markVolunteerAttendance(volunteer.getVolunteerId(), true);
            Toast.makeText(context, volunteer.getFullName() + " marked as Attended", Toast.LENGTH_SHORT).show();
        });

        holder.removeButton.setOnClickListener(v -> {
            // Remove volunteer from event and update Firestore
            removeVolunteerFromEvent(volunteer.getVolunteerId(), position);
            Toast.makeText(context, "Removed " + volunteer.getFullName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }

    private void markVolunteerAttendance(String volunteerId, boolean attended) {
        db.collection("events").document(eventId)
                .update("volunteersAttended", attended ? FieldValue.arrayUnion(volunteerId) : FieldValue.arrayRemove(volunteerId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeVolunteerFromEvent(String volunteerId, int position) {
        db.collection("events").document(eventId)
                .update("volunteers", FieldValue.arrayRemove(volunteerId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("users").document(volunteerId)
                                .update("eventsJoined", FieldValue.arrayRemove(eventId))
                                .addOnCompleteListener(removeFromUserTask -> {
                                    if (removeFromUserTask.isSuccessful()) {
                                        volunteers.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, volunteers.size());
                                    } else {
                                        Toast.makeText(context, "Failed to remove from user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Failed to remove volunteer from event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        TextView volunteerName, volunteerPhone;
        Button attendedButton, removeButton;

        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            volunteerName = itemView.findViewById(R.id.volunteerName);
            volunteerPhone = itemView.findViewById(R.id.volunteerPhone);
            attendedButton = itemView.findViewById(R.id.attendedButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
