package com.example.communityapp;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseRepository {

    private static final String USERS_COLLECTION = "users";
    private static final String EVENTS_COLLECTION = "events";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // ==========================
    // Event Functions (Create, Join, Delete, Remove Volunteer)
    // ==========================

    /**
     * Organizer creates a new event.
     * @param eventData Map of event details (title, description, etc.)
     */
    public void createEvent(Map<String, Object> eventData,
                            OnSuccessListener<DocumentReference> onSuccessListener,
                            OnFailureListener onFailureListener) {
        db.collection(EVENTS_COLLECTION)
                .add(eventData)
                .addOnSuccessListener(eventRef -> {
                    String eventId = eventRef.getId();
                    String organizerId = (String) eventData.get("organizerId");
                    // Add eventId to organizer's eventsCreated array
                    db.collection(USERS_COLLECTION).document(organizerId)
                            .update("eventsCreated", FieldValue.arrayUnion(eventId))
                            .addOnSuccessListener(aVoid -> onSuccessListener.onSuccess(eventRef))
                            .addOnFailureListener(onFailureListener);
                })
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Delete an event from Firestore (organizer's action).
     * @param eventId ID of the event to be deleted.
     */
    public void deleteEvent(String eventId,
                            OnSuccessListener<Void> onSuccessListener,
                            OnFailureListener onFailureListener) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .delete()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Join an event (volunteer action).
     * @param eventId ID of the event the volunteer is joining.
     * @param volunteerId ID of the volunteer.
     */
    public void joinEvent(String eventId, String volunteerId,
                          OnCompleteListener<Void> onCompleteListener,
                          OnFailureListener onFailureListener) {
        WriteBatch batch = db.batch();
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(eventId);
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(volunteerId);

        batch.update(eventRef, "volunteers", FieldValue.arrayUnion(volunteerId));
        batch.update(userRef, "eventsJoined", FieldValue.arrayUnion(eventId));

        batch.commit()
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Remove a volunteer from an event (volunteer unjoins).
     * @param eventId ID of the event.
     * @param volunteerId ID of the volunteer.
     */
    public void removeVolunteer(String eventId, String volunteerId,
                                OnCompleteListener<Void> onCompleteListener,
                                OnFailureListener onFailureListener) {
        WriteBatch batch = db.batch();
        DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(eventId);
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(volunteerId);

        batch.update(eventRef, "volunteers", FieldValue.arrayRemove(volunteerId));
        batch.update(userRef, "eventsJoined", FieldValue.arrayRemove(eventId));

        batch.commit()
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Fetch all events created by a specific organizer.
     * @param organizerId ID of the organizer.
     */
    public void getEventsCreatedByUser(String organizerId,
                                       OnSuccessListener<QuerySnapshot> onSuccessListener,
                                       OnFailureListener onFailureListener) {
        db.collection(EVENTS_COLLECTION)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Fetch all events in the system.
     */
    public void getAllEvents(OnSuccessListener<QuerySnapshot> onSuccessListener,
                             OnFailureListener onFailureListener) {
        db.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    /**
     * Fetch all events joined by a volunteer.
     * @param userId ID of the volunteer.
     */
    public void getEventsJoinedByUser(String userId,
                                      OnSuccessListener<QuerySnapshot> onSuccessListener,
                                      OnFailureListener onFailureListener) {
        db.collection(USERS_COLLECTION).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> eventIds = (List<String>) documentSnapshot.get("eventsJoined");
                        if (eventIds == null || eventIds.isEmpty()) {
                            // Return an empty QuerySnapshot-like object (fire dummy query)
                            db.collection(EVENTS_COLLECTION)
                                    .whereEqualTo("title", "__none__") // unlikely to match
                                    .get()
                                    .addOnSuccessListener(onSuccessListener)
                                    .addOnFailureListener(onFailureListener);
                            return;
                        }
                        db.collection(EVENTS_COLLECTION)
                                .whereIn(FieldPath.documentId(), eventIds)
                                .get()
                                .addOnSuccessListener(onSuccessListener)
                                .addOnFailureListener(onFailureListener);
                    } else {
                        onFailureListener.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

    // ==========================
    // User Profile Functions
    // ==========================

    public void updateUserProfile(Map<String, Object> data,
                                  OnSuccessListener<Void> onSuccessListener,
                                  OnFailureListener onFailureListener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (onFailureListener != null) onFailureListener.onFailure(new Exception("User not logged in"));
            return;
        }
        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .update(data)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void getUserProfile(String userId,
                               OnSuccessListener<DocumentSnapshot> onSuccessListener,
                               OnFailureListener onFailureListener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void getVolunteersForEvent(String eventId,
                                      OnSuccessListener<List<DocumentSnapshot>> onSuccessListener,
                                      OnFailureListener onFailureListener) {
        db.collection(EVENTS_COLLECTION)
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        List<String> volunteerIds = (List<String>) eventDoc.get("volunteers");
                        if (volunteerIds == null || volunteerIds.isEmpty()) {
                            onSuccessListener.onSuccess(new ArrayList<>()); // empty list
                            return;
                        }
                        // Fetch user documents for all volunteers
                        db.collection(USERS_COLLECTION)
                                .whereIn(FieldPath.documentId(), volunteerIds)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    onSuccessListener.onSuccess(querySnapshot.getDocuments());
                                })
                                .addOnFailureListener(onFailureListener);
                    } else {
                        onFailureListener.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(onFailureListener);
    }

}
