package net.tirgan.mex.utilities;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsUtils {

    public static void sendScreenImageName(Tracker aTracker, String aImageName) {
        // [START screen_view_hit]
        aTracker.setScreenName("Image~" + aImageName);
        aTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]
    }

    public static void sendScreenImageName(Tracker aTracker, String aImageName, String aLogTag) {
        Log.i(aLogTag, "Setting screen name: " + aImageName);
        sendScreenImageName(aTracker, aImageName);
    }

}
