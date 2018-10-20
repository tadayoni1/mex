package net.tirgan.mex.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.tirgan.mex.MyFirebaseApp;
import net.tirgan.mex.R;
import net.tirgan.mex.geofencing.Geofencing;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.detail.DetailActivity;
import net.tirgan.mex.ui.settings.SettingsActivity;
import net.tirgan.mex.ui.venue.VenueActivity;
import net.tirgan.mex.utilities.AnalyticsUtils;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.JobSchedulingUtils;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.SettingsUtil;

import java.util.Arrays;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity
        extends AppCompatActivity
        implements ListFragment.ListFragmentOnClickHandler, OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
        SortByDialogFragment.NoticeDialogListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 1;
    private static final int RC_IMAGE_CAPTURE_VENUE = 2;
    private static final int RC_LOCATION = 4;
    private static final int RC_VENUE = 5;

    private FragmentManager mFragmentManager;
    private ListFragment mListFragment;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    private boolean mIsEnabled;
    private Geofencing mGeofencing;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;

    private DatabaseReference mDatabaseReference;

    private GoogleMap mGoogleMap;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadListFragment();
                    return true;
                case R.id.navigation_maps:
                    loadMapsFragment();
                    return true;
            }
            return false;
        }
    };


    private static final long LOCATION_REFRESH_TIME = 60000;
    private static final float LOCATION_REFRESH_DISTANCE = 10.0f;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mGoogleMap != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private Tracker mTracker;
    private boolean mIsAnyVenueAdded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MiscUtils.LOLLIPOP_AND_HIGHER) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setExitTransition(fade);
        }
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        MyFirebaseApp application = (MyFirebaseApp) getApplication();
        mTracker = application.getDefaultTracker();

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }

        if (mFirebaseAuth.getUid() != null) {
            initializeActivity(savedInstanceState);
        }


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth aFirebaseAuth) {
                FirebaseUser currentUser = aFirebaseAuth.getCurrentUser();
                if (currentUser != null) {

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }

    private void initializeActivity(Bundle savedInstanceState) {
        String userId = mFirebaseAuth.getUid();
        mDatabaseReference = mDatabase.getReference().child(getString(R.string.users_database)).child(userId);

        mIsEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_enable_notifications), false);

        if (mIsEnabled) {
            mGeofencing = new Geofencing(this);
            JobSchedulingUtils.scheduleGeofencingRegister(this);
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFragmentManager = getSupportFragmentManager();

        mListFragment = new ListFragment();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.list_container, mListFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SettingsUtil.MENU_ITEM_SETTINGS, SettingsUtil.MENU_ITEM_SETTINGS, getString(R.string.menu_item_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case SettingsUtil.MENU_ITEM_SETTINGS:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void loadListFragment() {
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commit();
        AnalyticsUtils.sendScreenImageName(mTracker, ListFragment.class.getSimpleName(), LOG_TAG);
    }

    private void loadMapsFragment() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mapFragment)
                .commit();
        AnalyticsUtils.sendScreenImageName(mTracker, MapFragment.class.getSimpleName(), LOG_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else if (resultCode == RESULT_OK) {
                    initializeActivity(null);
                }
                break;
            case RC_VENUE:
                if (resultCode == Activity.RESULT_OK) {
                    Boolean result = data.getBooleanExtra(VenueActivity.RETURN_INTENT_EXTRA_IS_LOCATION_CHANGED, false);
                    if (result) {
                        if (mGeofencing != null) {
                            mGeofencing.updateGeofenceListAndRegisterAll(FirebaseDatabase.getInstance().getReference());
                        }
                    }
                }
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        AnalyticsUtils.sendScreenImageName(mTracker, MainActivity.class.getSimpleName(), LOG_TAG);
    }

    public void onAddNewVenueClick(View view) {
        Venue venue = new Venue("", "", FirebaseUtils.DEFAULT_RATING, FirebaseUtils.DEFAULT_LATITUDE, FirebaseUtils.DEFAULT_LONGITUDE);
        String key = mDatabaseReference.child(getString(R.string.venues_database)).push().getKey();
        mDatabaseReference.child(getString(R.string.venues_database)).child(key).setValue(venue);
        startVenueActivity(key, null);
        mListFragment.collapseFloatingActionMenu();
    }

    public void onAddNewEntryClick(View view) {
        if (mIsAnyVenueAdded) {
            MexEntry mexEntry = new MexEntry("", "", FirebaseUtils.DEFAULT_RATING, FirebaseUtils.DEFAULT_PRICE, FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL, new Date().getTime());
            String key = mDatabaseReference.child(getString(R.string.entries_database)).push().getKey();
            mDatabaseReference.child(getString(R.string.entries_database)).child(key).setValue(mexEntry);
            startDetailActivity(key);
            mListFragment.collapseFloatingActionMenu();
        } else {
            Toast.makeText(this, getString(R.string.first_add_venue), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @SuppressLint("RestrictedApi")
    private void startVenueActivity(String key, View aView) {
        Intent intent = new Intent(MainActivity.this, VenueActivity.class);
        intent.putExtra(VenueActivity.INTENT_EXTRA_FIREBASE_DATABASE_KEY, key);
        if (MiscUtils.LOLLIPOP_AND_HIGHER && aView != null) {
            aView.setTransitionName(getString(R.string.shared_element_venue_image_view));
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, aView, getString(R.string.shared_element_venue_image_view)).toBundle();
            if (mIsEnabled) {
                startActivityForResult(intent, RC_VENUE, bundle);
            } else {
                startActivity(intent, bundle);
            }
        } else {
            if (mIsEnabled) {
                startActivityForResult(intent, RC_VENUE);
            } else {
                startActivity(intent);
            }
        }
    }

    private void startDetailActivity(String key) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY, key);
        startActivity(intent);
    }


    @Override
    public void onVenueImageClick(String key, View aView) {
        startVenueActivity(key, aView);
    }


    @Override
    public void onSortByImageButtonClick() {
        showSortByDialog();
    }

    @Override
    public void isAnyVenueAdded(boolean aIsAnyVenueAdded) {
        mIsAnyVenueAdded = aIsAnyVenueAdded;
    }

    private void setLocation() {
        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_LOCATION, this)) {

            mDatabaseReference.child(getString(R.string.venues_database)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                    for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                        Venue venue = dataSnapshot.getValue(Venue.class);
                        LatLng latLng = new LatLng(venue.getLat(), venue.getLon());
                        mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(venue.getName()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError aDatabaseError) {

                }
            });

        }
    }

    @Override
    public void onMapReady(GoogleMap aGoogleMap) {
        mGoogleMap = aGoogleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        }
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.setOnMyLocationClickListener(this);
        setLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location aLocation) {

    }

    private void showSortByDialog() {
        DialogFragment dialogFragment = new SortByDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), SortByDialogFragment.class.getSimpleName());
        AnalyticsUtils.sendScreenImageName(mTracker, SortByDialogFragment.class.getSimpleName(), LOG_TAG);
    }


    @Override
    public void onDialogPositiveClick(int aSortBy, float aFilterByMinRating) {
        mListFragment.setSortAndFilter(aSortBy, aFilterByMinRating);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
