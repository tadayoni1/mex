package net.tirgan.mex.ui.venue;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.ui.settings.SettingsActivity;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.SettingsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VenueActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_FIREBASE_DATABASE_KEY = "intent-extra-firebase-database-key";
    public static final String RETURN_INTENT_EXTRA_IS_LOCATION_CHANGED = "return-intent-extra-is-location-changed";

    private static final int RC_IMAGE_CAPTURE_VENUE = 2;

    private static final int PICK_MAP_POINT_REQUEST = 1;

    @BindView(R.id.venue_iv)
    ImageView mVenueImageView;

    @BindView(R.id.venue_rb)
    RatingBar mRatingBar;

    @BindView(R.id.venue_et)
    EditText mEditText;

    private FirebaseDatabase mVenuesDatabase;

    private DatabaseReference mVenuesDatabaseReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private Venue mVenue;
    private boolean isLocationChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        ButterKnife.bind(this);

        Intent intentThatStartedThisActivity = getIntent();
        String key = intentThatStartedThisActivity.getStringExtra(INTENT_EXTRA_FIREBASE_DATABASE_KEY);

        initializeFirebase(key);

        initializeVenueDetails();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mVenue.setRating(rating);
                mVenuesDatabaseReference.setValue(mVenue);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mVenue.setName(mEditText.getText().toString());
                mVenuesDatabaseReference.setValue(mVenue);
            }
        });
    }

    @Override
    protected void onStop() {
        Intent returnIntent = new Intent();
        if (isLocationChanged) {
            returnIntent.putExtra(RETURN_INTENT_EXTRA_IS_LOCATION_CHANGED, true);
        } else {
            returnIntent.putExtra(RETURN_INTENT_EXTRA_IS_LOCATION_CHANGED, false);
        }
        setResult(Activity.RESULT_OK, returnIntent);
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, SettingsUtil.MENU_ITEM_SHARE, SettingsUtil.MENU_ITEM_SHARE, getString(R.string.menu_item_share)).setIcon(android.R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, SettingsUtil.MENU_ITEM_DELETE, SettingsUtil.MENU_ITEM_DELETE, getString(R.string.menu_item_delete)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, SettingsUtil.MENU_ITEM_SETTINGS, SettingsUtil.MENU_ITEM_SETTINGS, getString(R.string.menu_item_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case SettingsUtil.MENU_ITEM_DELETE:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this);
                }
                builder.setTitle(getString(R.string.delete_venue_title))
                        .setMessage(getString(R.string.delete_venue_dialog_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final String venueKey = mVenuesDatabaseReference.getKey();
                                String userId = FirebaseAuth.getInstance().getUid();
                                final DatabaseReference entriesDatabaseReference = FirebaseDatabase.getInstance()
                                        .getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database));
                                entriesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                                        for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                                            MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                                            if (mexEntry.getVenueKey().equals(venueKey)) {
                                                StorageReference entriesStorageReference = mFirebaseStorage.getReferenceFromUrl(mexEntry.getImageUrl());
                                                entriesStorageReference.delete();
                                                entriesDatabaseReference.child(dataSnapshot.getKey()).removeValue();
                                            }
                                        }
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError aDatabaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            case SettingsUtil.MENU_ITEM_SETTINGS:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }


    private void initializeFirebase(String aKey) {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mVenuesDatabase = FirebaseDatabase.getInstance();

        String userId = FirebaseAuth.getInstance().getUid();
        mVenuesDatabaseReference = mVenuesDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.venues_database)).child(aKey);
        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.venues_database));
    }

    private void initializeVenueDetails() {
        mVenuesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mVenue = aDataSnapshot.getValue(Venue.class);
                if (mVenue.getImageUri() != null && !mVenue.getImageUri().isEmpty()) {
                    Picasso.get().load(mVenue.getImageUri()).into(mVenueImageView);
                }
                mEditText.setText(mVenue.getName());
                mRatingBar.setRating(mVenue.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }


    public void onAddNewPhotoVenue(View view) {
        dispatchCameraIntent(RC_IMAGE_CAPTURE_VENUE);
    }


    private void dispatchCameraIntent(int aPermissionRequestId) {
        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, aPermissionRequestId, this)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra("return-data", true);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, aPermissionRequestId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_IMAGE_CAPTURE_VENUE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchCameraIntent(RC_IMAGE_CAPTURE_VENUE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_IMAGE_CAPTURE_VENUE:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    Uri selectedImageUri = MiscUtils.getImageUri(this, bitmap);
                    mVenueImageView.setImageBitmap(bitmap);
                    if (mVenue.getImageUri() != null && !mVenue.getImageUri().isEmpty()) {
                        mFirebaseStorage.getReferenceFromUrl(mVenue.getImageUri()).delete();
                    }
                    final StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
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
                                mVenue.setImageUri(downloadUri.toString());
                                mVenuesDatabaseReference.setValue(mVenue);
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
                break;
            case PICK_MAP_POINT_REQUEST:
                if (resultCode == RESULT_OK) {
                    LatLng latLng = data.getParcelableExtra(MapsActivity.RETURN_INTENT_EXTRA_PICKED_POINT);
                    mVenue.setLat(latLng.latitude);
                    mVenue.setLon(latLng.longitude);
                    mVenuesDatabaseReference.setValue(mVenue);
                }
        }
    }

    public void onPickLocationFromMap(View view) {
        Intent pickPointIntent = new Intent(this, MapsActivity.class);
        startActivityForResult(pickPointIntent, PICK_MAP_POINT_REQUEST);
        isLocationChanged = true;
    }
}
