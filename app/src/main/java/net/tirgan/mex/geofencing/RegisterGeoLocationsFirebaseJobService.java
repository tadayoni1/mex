package net.tirgan.mex.geofencing;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.tirgan.mex.R;

public class RegisterGeoLocationsFirebaseJobService
        extends JobService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private AsyncTask mBackgroundTask;
    private Geofencing mGeofencing;

    @Override
    public boolean onStartJob(final JobParameters job) {
        boolean isEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_enable_notifications), false);
        if (isEnabled) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            mBackgroundTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] aObjects) {
                    Context context = RegisterGeoLocationsFirebaseJobService.this;

                    mGeofencing = new Geofencing(context);
                    mGeofencing.updateGeofenceListAndRegisterAll(databaseReference);

                    return null;
                }

                @Override
                protected void onPostExecute(Object aO) {
                    jobFinished(job, false);
                }
            };
            mBackgroundTask.execute();
            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle aBundle) {

    }

    @Override
    public void onConnectionSuspended(int aI) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult aConnectionResult) {

    }
}
