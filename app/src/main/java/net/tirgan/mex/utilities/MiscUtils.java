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

import java.io.ByteArrayOutputStream;

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

    public static LatLng getLocation(Context aContext)
    {
        // Get the location manager
        LocationManager locationManager = (LocationManager) aContext.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        Double lat,lon;
        try {
            lat = location.getLatitude ();
            lon = location.getLongitude ();
            return new LatLng(lat, lon);
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }
}
