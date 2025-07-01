//app
package org.app;

import firebase.FirebaseConfig;
import ui.LoginScreen;
import util.ThemeManager;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Initialize theme first (this will also initialize fonts)
        ThemeManager.getInstance();

        // Initialize Firebase in a background thread to avoid blocking the UI
        Thread firebaseInitThread = new Thread(() -> {
            try {
                FirebaseConfig.initialize();
                System.out.println("Firebase initialized successfully");
            } catch (Exception e) {
                System.err.println("Firebase initialization error (app will work offline): " + e.getMessage());
            }
        });
        firebaseInitThread.setDaemon(true);
        firebaseInitThread.start();

        // Launch the UI immediately without waiting for Firebase
        SwingUtilities.invokeLater(() -> {
            new LoginScreen();
        });
    }
}