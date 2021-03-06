package net.tirgan.mex.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.util.TypedValue;

import com.esafirm.imagepicker.model.Image;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.ui.main.MexAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MiscUtils {

    public final static boolean LOLLIPOP_AND_HIGHER = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static Uri saveImageAndGetUri(Context aContext, Bitmap aBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        aBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(aContext.getContentResolver(), aBitmap, null, null);
        return Uri.parse(path);
    }

    public static Uri getImageUri(Context aContext, Bitmap aBitmap) {
        saveBitmapToCache(aContext, aBitmap);
        File imagePath = new File(aContext.getCacheDir(), aContext.getString(R.string.default_image_folder_in_cache));
        File file = new File(imagePath, aContext.getString(R.string.default_image_name_in_cache));
        Uri uri = FileProvider.getUriForFile(aContext, "net.tirgan.mex.fileprovider", file);
        return uri;
    }

    private static void saveBitmapToCache(Context aContext, Bitmap aBitmap) {
        try {
            File cachePath = new File(aContext.getCacheDir(), aContext.getString(R.string.default_image_folder_in_cache));
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/" + aContext.getString(R.string.default_image_name_in_cache)); // overwrites this image every time
            aBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Uri getImageUri(Context aContext, Image aImage) {
        File file = new File(aImage.getPath());
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(aContext.getContentResolver(), Uri.fromFile(file));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
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

    public static List<Pair<MexEntry, String>> sortMexEntries(List<Pair<MexEntry, String>> aMexEntryPairs, final int aSortBy) {
        Collections.sort(aMexEntryPairs, new Comparator<Pair<MexEntry, String>>() {
            @Override
            public int compare(Pair<MexEntry, String> aMexEntryPair1, Pair<MexEntry, String> aMexEntryPair2) {
                switch (aSortBy) {
                    case MexAdapter.SORT_BY_RATING:
                        return compareFloatsForSorting(aMexEntryPair2.first.getRating(), aMexEntryPair1.first.getRating());
                    case MexAdapter.SORT_BY_NAME:
                        return aMexEntryPair1.first.getName().compareTo(aMexEntryPair2.first.getName());
                    case MexAdapter.SORT_BY_ENTRY_DATE:
                        return 0;
                }
                return 0;
            }
        });

        return aMexEntryPairs;
    }

    private static int compareFloatsForSorting(float aF1, float aF2) {
        if (aF1 == aF2) {
            return 0;
        }
        if (aF1 > aF2) {
            return 1;
        } else {
            return -1;
        }
    }

    public static float getFloat(int aR, Context aContext) {
        TypedValue typedValue = new TypedValue();
        aContext.getResources().getValue(aR, typedValue, true);
        return typedValue.getFloat();
    }

    public static String getFormattedDate(long dateLong) {
        Date date = new Date(dateLong);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        return sdf.format(date);
    }

    public static boolean checkIfAnyOfLocationServicesIsEnabled(Context aContext) {
        LocationManager lm = (LocationManager) aContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        return gps_enabled || network_enabled;
    }


}
