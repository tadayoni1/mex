package net.tirgan.mex.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.tirgan.mex.R;
import net.tirgan.mex.ui.main.MainActivity;

public class NotificationUtils {

    private static final int GEOFENCING_PENDING_INTENT_ID = 1177;
    private static final String GEOFENCING_NOTIFICATION_CHANNEL_ID = "geofencing-notification-channel-id";
    private static final int GEOFENCING_NOTIFICATION_ID = 1188;

    public static void remindUserWhenEnteredARestaurant(final Context aContext, String aPlaceId) {
        final NotificationManager notificationManager = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);
        final String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                child(aContext.getString(R.string.users_database)).child(userId).child(aContext.getString(R.string.venues_database)).child(aPlaceId);

        GeoDataClient geoDataClient = Places.getGeoDataClient(aContext, null);
        geoDataClient.getPlaceById(aPlaceId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel mNotificationChannel = new NotificationChannel(
                                GEOFENCING_NOTIFICATION_CHANNEL_ID,
                                aContext.getString(R.string.geofencing_notification_channel_name),
                                NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(mNotificationChannel);
                    }

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(aContext, GEOFENCING_NOTIFICATION_CHANNEL_ID)
                            .setColor(ContextCompat.getColor(aContext, R.color.colorPrimary))
                            .setSmallIcon(R.drawable.map_marker)
                            .setLargeIcon(largeIcon(aContext))
                            .setContentTitle(aContext.getString(R.string.mex))
                            .setContentText(aContext.getString(R.string.format_notification_small, myPlace.getName()))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(aContext.getString(R.string.format_notification_big, myPlace.getName())))
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setContentIntent(contentIntent(aContext))
                            .setAutoCancel(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
                    }

                    notificationManager.notify(GEOFENCING_NOTIFICATION_ID, notificationBuilder.build());

                    places.release();
                }
            }
        });
    }


    private static PendingIntent contentIntent(Context aContext) {
        Intent startActivityIntent = new Intent(aContext, MainActivity.class);
        return PendingIntent.getActivity(aContext,
                GEOFENCING_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context aContext) {
        Resources resources = aContext.getResources();
        return BitmapFactory.decodeResource(resources, R.drawable.camera);
    }
}
