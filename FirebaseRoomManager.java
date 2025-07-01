package firebase;

import com.google.firebase.database.*;
import models.User;
import ui.MultiplayerStudyScreen;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseRoomManager {
    private static final DatabaseReference roomsRef = FirebaseDatabase.getInstance()
            .getReference("multiplayer_rooms");

    /**
     * Create a new room in Firebase
     * @param roomId the unique ID for the room
     * @param maxPlayers maximum number of players allowed in the room
     * @param creator the user who created the room
     * @return true if room creation was successful
     */
    public static boolean createRoom(String roomId, int maxPlayers, User creator) {
        try {
            // Check if room already exists
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] roomExists = {false};

            roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    roomExists[0] = snapshot.exists();
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    latch.countDown();
                }
            });

            // Wait for the check to complete with a timeout
            if (!latch.await(5, TimeUnit.SECONDS)) {
                System.err.println("Timeout checking if room exists");
                return false;
            }

            if (roomExists[0]) {
                System.out.println("Room " + roomId + " already exists, cannot create");
                return false; // Room already exists
            }

            // Create room data
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("maxPlayers", maxPlayers);
            roomData.put("createdAt", ServerValue.TIMESTAMP);
            roomData.put("creator", creator.getUsername());

            // First, create the room itself
            CountDownLatch roomCreationLatch = new CountDownLatch(1);
            final boolean[] roomCreationSuccessful = {false};

            roomsRef.child(roomId).setValue(roomData, (error, ref) -> {
                if (error != null) {
                    System.err.println("Error creating room: " + error.getMessage());
                    roomCreationSuccessful[0] = false;
                } else {
                    System.out.println("Successfully created room base data: " + roomId);
                    roomCreationSuccessful[0] = true;
                }
                roomCreationLatch.countDown();
            });

            // Wait for room creation to complete
            if (!roomCreationLatch.await(5, TimeUnit.SECONDS)) {
                System.err.println("Timeout creating room");
                return false;
            }

            if (!roomCreationSuccessful[0]) {
                return false;
            }

            // Now add the user to the room
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", creator.getUsername());
            userData.put("level", creator.getLevel());
            userData.put("joinedAt", ServerValue.TIMESTAMP);

            // Save user data to Firebase with a completion listener
            CountDownLatch userAddLatch = new CountDownLatch(1);
            final boolean[] userAddSuccessful = {false};

            roomsRef.child(roomId).child("users").child(creator.getUsername()).setValue(userData, (error, ref) -> {
                if (error != null) {
                    System.err.println("Error adding user to room: " + error.getMessage());
                    userAddSuccessful[0] = false;
                } else {
                    System.out.println("Successfully added user to room: " + roomId);
                    userAddSuccessful[0] = true;
                }
                userAddLatch.countDown();
            });

            // Wait for user addition to complete
            if (!userAddLatch.await(5, TimeUnit.SECONDS)) {
                System.err.println("Timeout adding user to room");
                return false;
            }

            return userAddSuccessful[0];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Join an existing room
     * @param roomId the ID of the room to join
     * @param user the user joining the room
     * @param forceJoin whether to join even if the room is in use
     * @return a result code: 0=success, 1=room doesn't exist, 2=room is full, 3=room in use by others
     */
    public static int joinRoom(String roomId, User user, boolean forceJoin) {
        try {
            // Check if room exists and has space
            CountDownLatch latch = new CountDownLatch(1);
            final int[] joinResult = {0}; // 0=success, 1=room doesn't exist, 2=room is full, 3=room in use by others
            final boolean[] roomHasOtherUsers = {false};

            roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        joinResult[0] = 1; // Room doesn't exist
                        latch.countDown();
                        return;
                    }

                    // Check if user is already in room
                    if (snapshot.child("users").child(user.getUsername()).exists()) {
                        joinResult[0] = 0; // Success - already in room
                        latch.countDown();
                        return;
                    }

                    // Check if room is full
                    long maxPlayers = snapshot.child("maxPlayers").getValue(Long.class);
                    long currentPlayers = snapshot.child("users").getChildrenCount();

                    if (currentPlayers >= maxPlayers) {
                        joinResult[0] = 2; // Room is full
                        latch.countDown();
                        return;
                    }

                    // Check if room is being used by others (and we're not forcing join)
                    if (!forceJoin && currentPlayers > 0) {
                        roomHasOtherUsers[0] = true;

                        // Get the names of users in the room for better error message
                        List<String> userNames = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
                            String username = userSnapshot.child("username").getValue(String.class);
                            if (username != null) {
                                userNames.add(username);
                            }
                        }

                        if (!userNames.isEmpty()) {
                            System.out.println("Room " + roomId + " is in use by: " + String.join(", ", userNames));
                            joinResult[0] = 3; // Room in use by others
                            latch.countDown();
                            return;
                        }
                    }

                    joinResult[0] = 0; // Success - can join
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    joinResult[0] = -1; // Error
                    latch.countDown();
                }
            });

            latch.await();

            if (joinResult[0] != 0) {
                return joinResult[0];
            }

            // Add user to room
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("level", user.getLevel());
            userData.put("joinedAt", ServerValue.TIMESTAMP);

            CountDownLatch joinLatch = new CountDownLatch(1);
            final boolean[] joinSuccessful = {false};

            roomsRef.child(roomId).child("users").child(user.getUsername()).setValue(userData, (error, ref) -> {
                if (error != null) {
                    System.err.println("Error joining room: " + error.getMessage());
                    joinSuccessful[0] = false;
                } else {
                    System.out.println("Successfully joined room: " + roomId);
                    joinSuccessful[0] = true;
                }
                joinLatch.countDown();
            });

            joinLatch.await(5, TimeUnit.SECONDS);
            return joinSuccessful[0] ? 0 : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Join an existing room (backward compatibility method)
     * @param roomId the ID of the room to join
     * @param user the user joining the room
     * @return true if joining was successful
     */
    public static boolean joinRoom(String roomId, User user) {
        int result = joinRoom(roomId, user, false);
        return result == 0;
    }

    /**
     * Leave a room
     * @param roomId the ID of the room to leave
     * @param username the username of the user leaving
     */
    public static void leaveRoom(String roomId, String username) {
        roomsRef.child(roomId).child("users").child(username).removeValue((error, ref) -> {
            if (error != null) {
                System.err.println("Error leaving room: " + error.getMessage());
            }
        });
    }

    /**
     * Set up a listener for room changes
     * @param roomId the ID of the room to listen to
     * @param screen the MultiplayerStudyScreen to update when users join/leave
     * @return the ValueEventListener that was created (store to remove later)
     */
    public static ValueEventListener listenForRoomChanges(String roomId, MultiplayerStudyScreen screen) {
        ValueEventListener roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(screen,
                                "This room no longer exists!",
                                "Room Closed",
                                JOptionPane.WARNING_MESSAGE);
                        screen.dispose();
                    });
                    return;
                }

                // Get list of users in the room
                List<String> usernames = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
                    usernames.add(userSnapshot.child("username").getValue(String.class));
                }

                // Update room info in the UI
                SwingUtilities.invokeLater(() -> {
                    screen.updateRoomInfo(usernames.size());

                    // Automatically refresh participants whenever room changes
                    screen.refreshRoomParticipants();
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Room listener cancelled: " + error.getMessage());
            }
        };

        roomsRef.child(roomId).addValueEventListener(roomListener);
        return roomListener;
    }

    /**
     * Remove room listener
     * @param roomId the ID of the room
     * @param listener the ValueEventListener to remove
     */
    public static void removeRoomListener(String roomId, ValueEventListener listener) {
        if (listener != null) {
            roomsRef.child(roomId).removeEventListener(listener);
        }
    }

    /**
     * Get users currently in a room
     * @param roomId the ID of the room
     * @return list of usernames in the room
     */
    public static List<String> getUsersInRoom(String roomId) {
        List<String> users = new ArrayList<>();
        try {
            CountDownLatch latch = new CountDownLatch(1);

            roomsRef.child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String username = userSnapshot.child("username").getValue(String.class);
                        if (username != null) {
                            users.add(username);
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    latch.countDown();
                }
            });

            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Delete all rooms where the specified user is the creator
     * This is used when a user logs in to clean up previous sessions
     *
     * @param username the username of the user
     * @return true if deletion completed successfully
     */
    public static boolean deleteUserRooms(String username) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] deletionSuccessful = {true};

            roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        System.out.println("No rooms found to delete for user: " + username);
                        latch.countDown();
                        return;
                    }

                    final CountDownLatch deletionLatch = new CountDownLatch(1);
                    final AtomicInteger pendingDeletions = new AtomicInteger(0);

                    // List to store rooms to be deleted
                    List<String> roomsToDelete = new ArrayList<>();

                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        // Check if user is in this room
                        if (roomSnapshot.child("users").child(username).exists()) {
                            roomsToDelete.add(roomSnapshot.getKey());
                        }
                    }

                    // If no rooms needed deletion
                    if (roomsToDelete.isEmpty()) {
                        deletionLatch.countDown();
                    } else {
                        pendingDeletions.set(roomsToDelete.size());

                        // Process each room deletion separately to avoid path conflicts
                        for (String roomId : roomsToDelete) {
                            roomsRef.child(roomId).removeValue((error, ref) -> {
                                if (error != null) {
                                    System.err.println("Error deleting room: " + error.getMessage());
                                    deletionSuccessful[0] = false;
                                } else {
                                    System.out.println("Successfully deleted room: " + roomId + " for user: " + username);
                                }

                                if (pendingDeletions.decrementAndGet() == 0) {
                                    deletionLatch.countDown();
                                }
                            });
                        }
                    }

                    // Wait for all deletions to complete
                    try {
                        deletionLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        System.err.println("Deletion wait interrupted: " + e.getMessage());
                        deletionSuccessful[0] = false;
                    }

                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Error cleaning up rooms: " + error.getMessage());
                    deletionSuccessful[0] = false;
                    latch.countDown();
                }
            });

            // Wait for the query to complete with timeout
            if (!latch.await(10, TimeUnit.SECONDS)) {
                System.err.println("Timeout waiting for room deletion to complete");
                return false;
            }

            // Add a small delay to ensure Firebase has time to process deletions
            Thread.sleep(500);

            return deletionSuccessful[0];
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a room is currently in use and by whom
     * @param roomId the ID of the room to check
     * @return A list of usernames currently in the room, empty if none
     */
    public static List<String> getUsersInRoomDetailed(String roomId) {
        List<String> users = new ArrayList<>();
        try {
            CountDownLatch latch = new CountDownLatch(1);

            roomsRef.child(roomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String username = userSnapshot.child("username").getValue(String.class);
                            Long joinedAt = userSnapshot.child("joinedAt").getValue(Long.class);
                            int level = 0;
                            if (userSnapshot.child("level").exists()) {
                                level = userSnapshot.child("level").getValue(Integer.class);
                            }

                            if (username != null) {
                                users.add(username + " (Level " + level + ")");
                            }
                        }
                    }
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Check if a room with the given ID already exists
     * @param roomId the ID of the room to check
     * @return true if the room exists, false otherwise
     */
    public static boolean roomExists(String roomId) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] exists = {false};

            roomsRef.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    exists[0] = snapshot.exists();
                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Error checking if room exists: " + error.getMessage());
                    latch.countDown();
                }
            });

            // Wait for the check to complete with a timeout
            if (!latch.await(3, TimeUnit.SECONDS)) {
                System.err.println("Timeout checking if room exists");
                return false;
            }

            return exists[0];
        } catch (Exception e) {
            System.err.println("Error checking if room exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}