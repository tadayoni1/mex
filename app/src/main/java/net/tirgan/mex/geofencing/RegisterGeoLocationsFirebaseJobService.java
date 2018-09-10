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
import com.google.android.gms.location.LocationServices;

import net.tirgan.mex.R;

public class RegisterGeoLocationsFirebaseJobService
        extends JobService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private AsyncTask mBackgroundTask;
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;
    private boolean mIsEnabled;

    @Override
    public boolean onStartJob(final JobParameters job) {
        mIsEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_enable_notifications), false);
        if (mIsEnabled) {
            mBackgroundTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] aObjects) {
                    Context context = RegisterGeoLocationsFirebaseJobService.this;


                    // Build up the LocationServices API client
                    // Uses the addApi method to request the LocationServices API
                    // Also uses enableAutoManage to automatically when to connect/suspend the client
                    mClient = new GoogleApiClient.Builder(context)
                            .addConnectionCallbacks(RegisterGeoLocationsFirebaseJobService.this)
                            .addOnConnectionFailedListener(RegisterGeoLocationsFirebaseJobService.this)
                            .addApi(LocationServices.API)
                            .build();

                    mGeofencing = new Geofencing(mClient, context);
                    mGeofencing.updateGeofenceListAndRegisterAll();

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
