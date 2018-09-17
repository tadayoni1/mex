package net.tirgan.mex.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.analytics.Tracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.tirgan.mex.MyFirebaseApp;
import net.tirgan.mex.R;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.model.Venues;
import net.tirgan.mex.utilities.AnalyticsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link MexWidgetProvider MexWidgetProvider} AppWidget.
 */
public class MexWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "net.tirgan.mex.widget.MexWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_PREFIX_VENUE_KEY = "appwidget_venue_";
    private Tracker mTracker;

    private Spinner mSpinner;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MexWidgetConfigureActivity.this;

            AnalyticsUtils.sendScreenImageName(mTracker, MexWidgetConfigureActivity.class.getSimpleName() + "-Add-Widget");

            // When the button is clicked, store the string locally
            String widgetText = mSpinner.getSelectedItem().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);
            Venues venues = (Venues) mSpinner.getSelectedItem();
            saveKeyPref(context, mAppWidgetId, venues.getFirebaseKey());

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            MexWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public MexWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveKeyPref(Context context, int appWidgetId, String key) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_VENUE_KEY + appWidgetId, key);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadKeyPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_VENUE_KEY + appWidgetId, null);
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Obtain the shared Tracker instance.
        MyFirebaseApp application = (MyFirebaseApp) getApplication();
        mTracker = application.getDefaultTracker();

        AnalyticsUtils.sendScreenImageName(mTracker, MexWidgetConfigureActivity.class.getSimpleName());

        setContentView(R.layout.mex_widget_configure);
        mSpinner = findViewById(R.id.ap_mex_conf_spinner);

        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String key = loadKeyPref(MexWidgetConfigureActivity.this, mAppWidgetId);
        if (mSpinner.getAdapter() != null) {
            for (int i = 0; i < mSpinner.getAdapter().getCount(); i++) {
                Venues venues = (Venues) mSpinner.getItemAtPosition(i);
                if (venues.getFirebaseKey().equals(key)) {
                    mSpinner.setSelection(i);
                    break;
                }
            }
        }

        final List<Venues> venues = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference venuesDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.venues_database));
        venuesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    Venue venue = dataSnapshot.getValue(Venue.class);
                    venues.add(new Venues(dataSnapshot.getKey(), venue.getName()));
                }
                ArrayAdapter<Venues> spinnerArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, venues);
                mSpinner.setAdapter(spinnerArrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });

    }
}

