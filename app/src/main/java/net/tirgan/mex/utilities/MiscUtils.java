package net.tirgan.mex.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;

import com.esafirm.imagepicker.model.Image;

import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.main.VenuesAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

    public static Uri getImageUri(Context aContext, Image aImage) {
        File file = new File(aImage.getPath());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(aContext.getContentResolver(), Uri.fromFile(file));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            return getImageUri(aContext, compressedBitmap);
        } catch (IOException aE) {
            aE.printStackTrace();
        }
        return null;
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
