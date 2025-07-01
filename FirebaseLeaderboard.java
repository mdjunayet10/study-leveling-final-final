//firebase->FirebaseLeaderboard

package firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import models.User;

public class FirebaseLeaderboard {

    public static void uploadUserStats(User user) {
        // Use the totalCompletedTasks counter instead of counting current tasks
        // This ensures deleted tasks that were completed are still counted
        int completed = user.getTotalCompletedTasks();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("leaderboard")
                .child(user.getUsername()); // Use username as key

        ref.child("level").setValueAsync(user.getLevel());
        ref.child("xp").setValueAsync(user.getXp());
        ref.child("completedTasks").setValueAsync(completed);
    }
}