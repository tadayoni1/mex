package net.tirgan.mex;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Disk Persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
