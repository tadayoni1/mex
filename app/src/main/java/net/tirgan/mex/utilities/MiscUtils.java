package net.tirgan.mex.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.main.VenuesAdapter;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class MiscUtils {

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

    public static LatLng getLocation(Context aContext) {
        // Get the location manager
        LocationManager locationManager = (LocationManager) aContext.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        Double lat, lon;
        try {
            lat = location.getLatitude();
            lon = location.getLongitude();
            return new LatLng(lat, lon);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Venue> sortVenues(List<Venue> aVenues, final int aSortBy) {
        Collections.sort(aVenues, new Comparator<Venue>() {
            @Override
            public int compare(Venue aVenue1, Venue aVenue2) {
                switch (aSortBy) {
                    case VenuesAdapter.SORT_BY_RATING:
                        return (int) (aVenue1.getRating() - aVenue2.getRating());
                    case VenuesAdapter.SORT_BY_NAME:
                        return aVenue1.getName().compareTo(aVenue2.getName());
                    case VenuesAdapter.SORT_BY_ENTRY_DATE:
                        return 0;
                }
                return 0;
            }
        });
     return aVenues;
    }

}
