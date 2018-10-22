package net.tirgan.mex.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.tirgan.mex.R;
import net.tirgan.mex.ui.main.VenuesAdapter;

public class SettingsUtil {

    public final static int MENU_ITEM_SHARE = 0;
    public final static int MENU_ITEM_DELETE = 9;
    public final static int MENU_ITEM_SETTINGS = 10;


    public final static String PREF_SORT_BY = "pref-sort-by";
    public final static String PREF_FILTER_BY = "pref-filter-by";

    public final static String PREF_AD_ENABLED = "pref-ad-enabled";


    public static void savePrefSortAndFilter(Context aContext, int aSortBy, float aFilterBy) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(aContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SettingsUtil.PREF_SORT_BY, aSortBy);
        editor.putFloat(SettingsUtil.PREF_FILTER_BY, aFilterBy);
        editor.apply();
    }

    public static int readPrefSort(Context aContext) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(aContext);
        return sp.getInt(SettingsUtil.PREF_SORT_BY, VenuesAdapter.SORT_BY_ENTRY_DATE);
    }

    public static int findIdForRadioButton(int aSortBy) {
        switch (aSortBy) {
            case VenuesAdapter.SORT_BY_ENTRY_DATE:
                return R.id.sort_by_entry_date;
            case VenuesAdapter.SORT_BY_NAME:
                return R.id.sort_by_name_rb;
            case VenuesAdapter.SORT_BY_RATING:
                return R.id.sort_by_rating_rb;
        }
        return R.id.sort_by_entry_date;
    }

    public static float readPrefFilter(Context aContext) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(aContext);
        return sp.getFloat(SettingsUtil.PREF_FILTER_BY, 0);
    }
}
