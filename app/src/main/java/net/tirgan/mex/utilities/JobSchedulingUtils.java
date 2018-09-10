package net.tirgan.mex.utilities;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import net.tirgan.mex.geofencing.RegisterGeoLocationsFirebaseJobService;

public class JobSchedulingUtils {

    private static final int GEOFENCING_REGISTER_INTERVAL_HOURS = 20;
    private static final int GEOFENCING_REGISTER_INTERVAL_SECONDS = GEOFENCING_REGISTER_INTERVAL_HOURS * 60 * 60;
    private static final int SYNC_FLEXTIME_SECONDS = 4 * 60 * 60;

    private static final String GEOFENCING_REGISTER_JOB_TAG = "geofencing-register-job-tag";

    private static boolean sInitialized;

    synchronized public static void scheduleGeofencingRegister(@NonNull final Context aContext) {
        if (sInitialized) {
            return;
        }
        Driver driver = new GooglePlayDriver(aContext);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job constraintGeoFencingRegisterJob = jobDispatcher.newJobBuilder()
                .setService(RegisterGeoLocationsFirebaseJobService.class)
                .setTag(GEOFENCING_REGISTER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(GEOFENCING_REGISTER_INTERVAL_SECONDS, GEOFENCING_REGISTER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        jobDispatcher.schedule(constraintGeoFencingRegisterJob);
        sInitialized = true;
    }
}
