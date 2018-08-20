package net.tirgan.mex.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkUtils {

    public static final String BAKING_APP_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";


    public static void showNoNetworkToast(Context aContext) {
        Toast.makeText(aContext, "No Internet connection is available.", Toast.LENGTH_SHORT).show();
    }

    public static boolean isOnline(Context aContext) {
        ConnectivityManager cm =
                (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
