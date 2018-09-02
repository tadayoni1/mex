package net.tirgan.mex.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.detail.DetailActivity;
import net.tirgan.mex.ui.venue.VenueActivity;
import net.tirgan.mex.utilities.MiscUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity
        extends AppCompatActivity
        implements ListFragment.ListFragmentOnClickHandler {


    private static final int RC_SIGN_IN = 1;
    private static final int RC_IMAGE_CAPTURE_VENUE = 2;
    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;

    private FragmentManager mFragmentManager;
    private ListFragment mListFragment;
    private SupportMapFragment mMapFragment;

    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mDatabase;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;


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


    private void dispatchTakePictureIntent(int activity) {
        if (checkPermissionsAndRequest(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, activity)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra("return-data", true);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, activity);
            }
        }
    }

    private void loadListFragment() {
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mListFragment)
                .commit();
    }

    private void loadMapsFragment() {
        mFragmentManager.beginTransaction()
                .replace(R.id.list_container, mMapFragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        String userId = mFirebaseAuth.getUid();
        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId);
        mDatabaseReference = mDatabase.getReference().child(getString(R.string.users_database)).child(userId);

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFragmentManager = getSupportFragmentManager();

        mListFragment = new ListFragment();
        mMapFragment = SupportMapFragment.newInstance();

        mFragmentManager.beginTransaction()
                .add(R.id.list_container, mListFragment)
                .commit();


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
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = MiscUtils.getImageUri(this, (Bitmap) data.getExtras().get("data"));
                    final StorageReference photoRef = mStorageReference.child(getString(R.string.venues_database)).child(selectedImageUri.getLastPathSegment());
                    UploadTask uploadTask = photoRef.putFile(selectedImageUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> aTask) throws Exception {
                            if (!aTask.isSuccessful()) {
                                throw aTask.getException();
                            }

                            // Continue with the task to get the download URL
                            return photoRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> aTask) {
                            if (aTask.isSuccessful()) {
                                // When the image has successfully uploaded, we get its download URL
                                Uri downloadUri = aTask.getResult();
                                Venue venue = new Venue("TODO", downloadUri.toString(), 0, 0, 0);
                                String key = mDatabaseReference.child(getString(R.string.venues_database)).push().getKey();
                                mDatabaseReference.child(getString(R.string.venues_database)).child(key).setValue(venue);
                                startVenueActivity(key);
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
                break;
            case RC_IMAGE_CAPTURE_MEX_ENTRY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = MiscUtils.getImageUri(this, (Bitmap) data.getExtras().get("data"));
                    final StorageReference photoRef = mStorageReference.child(getString(R.string.venues_database)).child(selectedImageUri.getLastPathSegment());
                    UploadTask uploadTask = photoRef.putFile(selectedImageUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> aTask) throws Exception {
                            if (!aTask.isSuccessful()) {
                                throw aTask.getException();
                            }

                            // Continue with the task to get the download URL
                            return photoRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> aTask) {
                            if (aTask.isSuccessful()) {
                                // When the image has successfully uploaded, we get its download URL
                                Uri downloadUri = aTask.getResult();
                                MexEntry mexEntry = new MexEntry("", "TODO", 2.5f, 10.00f, downloadUri.toString());
                                String key = mDatabaseReference.child(getString(R.string.entries_database)).push().getKey();
                                mDatabaseReference.child(getString(R.string.entries_database)).child(key).setValue(mexEntry);
                                startDetailActivity(key);
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });


//                    MexEntry entry = new MexEntry("-LLJ7973_z7-7aIyvK3w", "Pizza", 3.4f, 10.5f, "https://s3-media1.fl.yelpcdn.com/bphoto/DtEMGISoO83Z5WjpWWNMiA/o.jpg");
//                    String userId = mFirebaseAuth.getUid();
//                    mDatabaseReference.child(getString(R.string.entries_database)).push().setValue(entry);
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
    }

    public void onAddNewVenueClick(View view) {
        dispatchTakePictureIntent(RC_IMAGE_CAPTURE_VENUE);
    }

    public void onAddNewEntryClick(View view) {
        dispatchTakePictureIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
    }

    // Permissions
    private boolean checkPermissionsAndRequest(String[] aPermissions, int aPermissionRequestId) {
        if (checkPermissions(aPermissions)) {
            return true;
        } else {
            requestPermission(aPermissions, aPermissionRequestId);
            return false;
        }
    }

    private boolean checkPermissions(String[] aPermissions) {
        for (String aPermission : aPermissions) {
            if (ContextCompat.checkSelfPermission(this,
                    aPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }
        return true;
    }

    private void requestPermission(String[] aPermissions, int aPermissionRequestId) {
        ActivityCompat.requestPermissions(this,
                aPermissions,
                aPermissionRequestId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC_IMAGE_CAPTURE_VENUE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent(RC_IMAGE_CAPTURE_VENUE);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case RC_IMAGE_CAPTURE_MEX_ENTRY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
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
}
