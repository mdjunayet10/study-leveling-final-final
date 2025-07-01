//util->FirebaseManager
package util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import models.User;

public class FirebaseManager {

    public static void uploadUserStats(User user) {
        try {
            // Use the totalCompletedTasks counter instead of counting current tasks
            int completedTasks = user.getTotalCompletedTasks();

            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("leaderboard")
                    .child(user.getUsername());  // Use username as unique key

            dbRef.child("level").setValueAsync(user.getLevel());
            dbRef.child("xp").setValueAsync(user.getXp());
            dbRef.child("completedTasks").setValueAsync(completedTasks);

            System.out.println("✅ Successfully uploaded user stats to Firebase.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to upload stats.");
        }
    }

    /**
     * Downloads user statistics from Firebase and updates the local user object
     * @param user The user to update with data from Firebase
     */
    public static void downloadUserStats(User user) {
        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference("leaderboard")
                    .child(user.getUsername());

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get values from Firebase
                        Long level = dataSnapshot.child("level").getValue(Long.class);
                        Long xp = dataSnapshot.child("xp").getValue(Long.class);
                        Long completedTasks = dataSnapshot.child("completedTasks").getValue(Long.class);

                        // Update local user if data exists
                        if (level != null) {
                            user.setLevel(level.intValue());
                        }
                        if (xp != null) {
                            user.setXp(xp.intValue());
                        }
                        if (completedTasks != null) {
                            // Set the total completed tasks counter
                            user.setTotalCompletedTasks(completedTasks.intValue());
                        }

                        // Save the updated user locally
                        DataManager.saveUser(user);
                        System.out.println("✅ Successfully downloaded and updated user stats from Firebase.");
                    } else {
                        System.out.println("ℹ️ No data found in Firebase for user: " + user.getUsername());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("❌ Failed to download stats: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Failed to download stats.");
        }
    }
}