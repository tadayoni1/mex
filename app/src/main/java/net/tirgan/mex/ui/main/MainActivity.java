package net.tirgan.mex.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.detail.DetailActivity;
import net.tirgan.mex.ui.settings.SettingsActivity;
import net.tirgan.mex.ui.venue.VenueActivity;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.SettingsUtil;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity
        extends AppCompatActivity
        implements ListFragment.ListFragmentOnClickHandler, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {


    private static final int RC_SIGN_IN = 1;
    private static final int RC_IMAGE_CAPTURE_VENUE = 2;
    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;
    private static final int RC_LOCATION = 4;

    private FragmentManager mFragmentManager;
    private ListFragment mListFragment;
    private SupportMapFragment mMapFragment;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //    private FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mDatabase;

    //    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private GoogleMap mGoogleMap;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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
    private LocationManager mLocationManager;
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


//    private void dispatchTakePictureIntent(int aPermissionRequestId) {
//        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, aPermissionRequestId, this)) {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            takePictureIntent.putExtra("return-data", true);
//            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(takePictureIntent, aPermissionRequestId);
//            }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
//        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        String userId = mFirebaseAuth.getUid();
//        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId);
        mDatabaseReference = mDatabase.getReference().child(getString(R.string.users_database)).child(userId);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFragmentManager = getSupportFragmentManager();

        mListFragment = new ListFragment();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.list_container, mListFragment)
                    .commit();
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
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean isNotificationEnabled = sharedPreferences.getBoolean(getString(R.string.settings_enable_notifications), false);
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
    }

    private void loadMapsFragment() {
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mMapFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            case RC_IMAGE_CAPTURE_VENUE:
//                if (resultCode == RESULT_OK) {
//                    Uri selectedImageUri = MiscUtils.getImageUri(this, (Bitmap) data.getExtras().get("data"));
//                    final StorageReference photoRef = mStorageReference.child(getString(R.string.venues_database)).child(selectedImageUri.getLastPathSegment());
//                    UploadTask uploadTask = photoRef.putFile(selectedImageUri);
//                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                        @Override
//                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> aTask) throws Exception {
//                            if (!aTask.isSuccessful()) {
//                                throw aTask.getException();
//                            }
//
//                            // Continue with the task to get the download URL
//                            return photoRef.getDownloadUrl();
//                        }
//                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> aTask) {
//                            if (aTask.isSuccessful()) {
//                                // When the image has successfully uploaded, we get its download URL
//                                Uri downloadUri = aTask.getResult();
////                                Venue venue = new Venue("TODO", downloadUri.toString(), 0, 0, 0);
////                                String key = mDatabaseReference.child(getString(R.string.venues_database)).push().getKey();
////                                mDatabaseReference.child(getString(R.string.venues_database)).child(key).setValue(venue);
////                                startVenueActivity(key);
//                            } else {
//                                // Handle failures
//                                // ...
//                            }
//                        }
//                    });
//                }
//                break;
//            case RC_IMAGE_CAPTURE_MEX_ENTRY:
//                if (resultCode == RESULT_OK) {
//                    Uri selectedImageUri = MiscUtils.getImageUri(this, (Bitmap) data.getExtras().get("data"));
//                    final StorageReference photoRef = mStorageReference.child(getString(R.string.entries_database)).child(selectedImageUri.getLastPathSegment());
//                    UploadTask uploadTask = photoRef.putFile(selectedImageUri);
//                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                        @Override
//                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> aTask) throws Exception {
//                            if (!aTask.isSuccessful()) {
//                                throw aTask.getException();
//                            }
//
//                            // Continue with the task to get the download URL
//                            return photoRef.getDownloadUrl();
//                        }
//                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> aTask) {
//                            if (aTask.isSuccessful()) {
//                                // When the image has successfully uploaded, we get its download URL
//                                Uri downloadUri = aTask.getResult();
////                                MexEntry mexEntry = new MexEntry("", "TODO", 2.5f, 10.00f, downloadUri.toString());
////                                String key = mDatabaseReference.child(getString(R.string.entries_database)).push().getKey();
////                                mDatabaseReference.child(getString(R.string.entries_database)).child(key).setValue(mexEntry);
////                                startDetailActivity(key);
//                            } else {
//                                // Handle failures
//                                // ...
//                            }
//                        }
//                    });
//
//
////                    MexEntry entry = new MexEntry("-LLJ7973_z7-7aIyvK3w", "Pizza", 3.4f, 10.5f, "https://s3-media1.fl.yelpcdn.com/bphoto/DtEMGISoO83Z5WjpWWNMiA/o.jpg");
////                    String userId = mFirebaseAuth.getUid();
////                    mDatabaseReference.child(getString(R.string.entries_database)).push().setValue(entry);
//                }
//
//                break;
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
        mListFragment.reloadData();
    }

    public void onAddNewVenueClick(View view) {
//        dispatchTakePictureIntent(RC_IMAGE_CAPTURE_VENUE);
        Venue venue = new Venue("", "", 2.5f, 0, 0);
        String key = mDatabaseReference.child(getString(R.string.venues_database)).push().getKey();
        mDatabaseReference.child(getString(R.string.venues_database)).child(key).setValue(venue);
        startVenueActivity(key);

    }

    public void onAddNewEntryClick(View view) {
//        dispatchTakePictureIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
        MexEntry mexEntry = new MexEntry("", "", 2.5f, 10.00f, "");
        String key = mDatabaseReference.child(getString(R.string.entries_database)).push().getKey();
        mDatabaseReference.child(getString(R.string.entries_database)).child(key).setValue(mexEntry);
        startDetailActivity(key);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void startVenueActivity(String key) {
        Intent intent = new Intent(MainActivity.this, VenueActivity.class);
        intent.putExtra(VenueActivity.INTENT_EXTRA_FIREBASE_DATABASE_KEY, key);
        startActivity(intent);
    }

    private void startDetailActivity(String key) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY, key);
        startActivity(intent);
    }

    @Override
    public void onVenueImageClick(String key) {

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

//            LatLng currentLocation = MiscUtils.getLocation(this);
//            if (currentLocation != null) {
//                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
//            }
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
}
