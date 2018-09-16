package net.tirgan.mex.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;

import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.main.VenuesAdapter;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MiscUtils {

    public final static boolean LOLLIPOP_AND_HIGHER = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static Uri getImageUri(Context aContext, Bitmap aBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        aBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(aContext.getContentResolver(), aBitmap, null, null);
        return Uri.parse(path);
    }

    public static boolean checkPermissionsAndRequest(String[] aPermissions, int aPermissionRequestId, Context aContext) {
        if (checkPermissions(aPermissions, aContext)) {
            return true;
        } else {
            requestPermission(aPermissions, aPermissionRequestId, aContext);
            return false;
        }
    }

    public static boolean checkPermissions(String[] aPermissions, Context aContext) {
        for (String aPermission : aPermissions) {
            if (ContextCompat.checkSelfPermission(aContext,
                    aPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }
        return true;
    }

    public static void requestPermission(String[] aPermissions, int aPermissionRequestId, Context aContext) {
        ActivityCompat.requestPermissions((Activity) aContext,
                aPermissions,
                aPermissionRequestId);
    }

//    public static LatLng getLocation(Context aContext) {
//        // Get the location manager
//        LocationManager locationManager = (LocationManager) aContext.getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String bestProvider = locationManager.getBestProvider(criteria, false);
//        Location location = locationManager.getLastKnownLocation(bestProvider);
//        Double lat, lon;
//        try {
//            lat = location.getLatitude();
//            lon = location.getLongitude();
//            return new LatLng(lat, lon);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public static List<Pair<Venue, String>> sortVenues(List<Pair<Venue, String>> aVenuePairs, final int aSortBy) {
        Collections.sort(aVenuePairs, new Comparator<Pair<Venue, String>>() {
            @Override
            public int compare(Pair<Venue, String> aVenuePair1, Pair<Venue, String> aVenuePair2) {
                switch (aSortBy) {
                    case VenuesAdapter.SORT_BY_RATING:
                        return (int) (aVenuePair2.first.getRating() - aVenuePair1.first.getRating());
                    case VenuesAdapter.SORT_BY_NAME:
                        return aVenuePair1.first.getName().compareTo(aVenuePair2.first.getName());
                    case VenuesAdapter.SORT_BY_ENTRY_DATE:
                        return 0;
                }
                return 0;
            }
        });

        return aVenuePairs;
    }

}
