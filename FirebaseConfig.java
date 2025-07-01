//firebase->FirebaseConfig
package firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseConfig {
    private static FirebaseAuth firebaseAuth;
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private static final CompletableFuture<Boolean> initializationFuture = new CompletableFuture<>();
    private static final int CONNECTION_TIMEOUT_MS = 5000; // 5 seconds timeout

    public static void initialize() {
        if (isInitialized.get()) {
            return; // Already initialized
        }

        try {
            // Load the service account from resources using classloader
            InputStream serviceAccount = FirebaseConfig.class.getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                System.err.println("❌ serviceAccountKey.json not found in resources.");
                initializationFuture.complete(false);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://study-leveling-default-rtdb.asia-southeast1.firebasedatabase.app/")  // replace with your DB URL
                    .setConnectTimeout(CONNECTION_TIMEOUT_MS) // Add timeout to prevent hanging
                    .build();

            FirebaseApp.initializeApp(options);
            firebaseAuth = FirebaseAuth.getInstance();
            isInitialized.set(true);
            initializationFuture.complete(true);
            System.out.println("✅ Firebase initialized successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Firebase.");
            e.printStackTrace();
            initializationFuture.complete(false);
        }
    }

    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static boolean isInitialized() {
        return isInitialized.get();
    }

    public static CompletableFuture<Boolean> getInitializationFuture() {
        return initializationFuture;
    }
}