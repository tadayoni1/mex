package net.tirgan.mex.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import net.tirgan.mex.utilities.NotificationUtils;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == GeofencingRequest.INITIAL_TRIGGER_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence: triggeringGeofences) {
                String placeId = geofence.getRequestId();
                NotificationUtils.remindUserWhenEnteredARestaurant(context, placeId);
            }
        }


    }
}
