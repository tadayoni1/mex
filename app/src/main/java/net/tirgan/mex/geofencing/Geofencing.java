package net.tirgan.mex.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback<Status>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;
    private static final float GEOFENCE_RADIUS = 500.0f;
    private final GoogleApiClient mGoogleApiClient;
    private final Context mContext;

    private PendingIntent mGeofencePendingIntent;
    private List<Geofence> mGeofenceList;

    public Geofencing(Context aContext) {
        // Build up the LocationServices API client
        // Uses the addApi method to request the LocationServices API
        // Also uses enableAutoManage to automatically when to connect/suspend the client
        mGoogleApiClient = new GoogleApiClient.Builder(aContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
        mContext = aContext;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }


    private void registerAllGeofences() {
        if (mGoogleApiClient == null || mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e) {

        }
    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                    getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e) {

        }
    }

    // Database reference has to be passed. If the getReference() call is made inside this method it will fail inside the AsyncTaskView in RegisterGeoLocationsFirebaseJobService in onStartJob
    public void updateGeofenceListAndRegisterAll(DatabaseReference aDatabaseReference) {
        mGeofenceList = new ArrayList<>();
        final String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = aDatabaseReference.child(mContext.getString(R.string.users_database)).child(userId).child(mContext.getString(R.string.entries_database));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                ArrayList<String> placeIds = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    if (!placeIds.contains(mexEntry.getPlaceId())) {
                        placeIds.add(mexEntry.getPlaceId());
                    }
                }
                GeoDataClient geoDataClient = Places.getGeoDataClient(mContext, null);
                for (final String placeId : placeIds) {
                    if (placeId != null && !placeId.isEmpty()) {
                        geoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                if (task.isSuccessful()) {
                                    PlaceBufferResponse places = task.getResult();
                                    Place myPlace = places.get(0);
                                    Geofence geofence = new Geofence.Builder()
                                            .setRequestId(placeId)
                                            .setExpirationDuration(GEOFENCE_TIMEOUT)
                                            .setCircularRegion(myPlace.getLatLng().latitude, myPlace.getLatLng().longitude, GEOFENCE_RADIUS)
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                            .build();
                                    mGeofenceList.add(geofence);
                                }
                            }
                        });
                    }
                }

                registerAllGeofences();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
            mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Status aStatus) {

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
