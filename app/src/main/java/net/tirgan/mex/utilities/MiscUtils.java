package net.tirgan.mex.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

public class MiscUtils {

    public static Uri getImageUri(Context aContext, Bitmap aBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        aBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(aContext.getContentResolver(), aBitmap, null, null);
        return Uri.parse(path);
    }
}
