package net.tirgan.mex.ui.detail;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
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
import com.squareup.picasso.Target;

import net.tirgan.mex.MyFirebaseApp;
import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.model.Venues;
import net.tirgan.mex.ui.settings.SettingsActivity;
import net.tirgan.mex.utilities.AnalyticsUtils;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.SettingsUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY = "intent-extra-detail-firebase-database-key";

    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;

    private static final String INSTANCE_STATE_MEX_ENTRY = "instance-state-mex-entry";


    @BindView(R.id.detail_iv)
    ImageView mDetailImageView;

    @BindView(R.id.detail_rb)
    RatingBar mDetailRatingBar;

    @BindView(R.id.detail_et)
    EditText mDetailEditText;

    @BindView(R.id.detail_price_et)
    EditText mDetailPriceEditText;

    @BindView(R.id.detail_venue_spinner)
    Spinner mVenueSpinner;

    private FirebaseDatabase mDetailDatabase;

    private DatabaseReference mDetailDatabaseReference;
    private DatabaseReference mVenuesDatabaseReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;


    private MexEntry mMexEntry;
    private String mKey;
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MiscUtils.LOLLIPOP_AND_HIGHER) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);
        }
        setContentView(R.layout.activity_detail);

        if (MiscUtils.LOLLIPOP_AND_HIGHER) {
            supportPostponeEnterTransition();
        }

        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        MyFirebaseApp application = (MyFirebaseApp) getApplication();
        mTracker = application.getDefaultTracker();

        if (savedInstanceState != null) {
            MexEntry mexEntry = savedInstanceState.getParcelable(INSTANCE_STATE_MEX_ENTRY);
            if (mexEntry != null) {
                mMexEntry = mexEntry;
            }
        }

        Intent intentThatStartedThisActivity = getIntent();
        mKey = intentThatStartedThisActivity.getStringExtra(INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY);

        initializeFirebase();

        initializeMexEntryDetails();
        initializeVenuesSpinner();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDetailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mMexEntry.setName(mDetailEditText.getText().toString());
                mDetailDatabaseReference.setValue(mMexEntry);
            }
        });

        mDetailPriceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float price = 0f;
                try {
                    price = Float.valueOf(mDetailPriceEditText.getText().toString());
                } catch (Exception e) {

                }
                mMexEntry.setPrice(price);
                mDetailDatabaseReference.setValue(mMexEntry);
            }
        });

        mDetailRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mMexEntry.setRating(rating);
                mDetailDatabaseReference.setValue(mMexEntry);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INSTANCE_STATE_MEX_ENTRY, mMexEntry);
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
                builder.setTitle(getString(R.string.delete_entry_dialog_title))
                        .setMessage(getString(R.string.delete_entry_dialog_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseStorage.getInstance().getReferenceFromUrl(mMexEntry.getImageUrl()).delete();
                                mDetailDatabaseReference.removeValue();
                                finish();
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
            case SettingsUtil.MENU_ITEM_SHARE:
                AnalyticsUtils.sendScreenImageName(mTracker, DetailActivity.class.getSimpleName() + "-share");
                Picasso.get()
                        .load(mMexEntry.getImageUrl())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, MiscUtils.getImageUri(getApplicationContext(), bitmap));
                                shareIntent.setType("image/jpeg");
                                startActivity(Intent.createChooser(shareIntent, getString(R.string.send_to)));
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                Toast.makeText(getApplication(), getString(R.string.failed_to_download), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
                break;

        }
        return true;
    }

    private void initializeFirebase() {
        mDetailDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        String userId = FirebaseAuth.getInstance().getUid();
        mDetailDatabaseReference = mDetailDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database)).child(mKey);
        mVenuesDatabaseReference = mDetailDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.venues_database));

        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database));
    }

    private void initializeVenuesSpinner() {
        final List<Venues> venues = new ArrayList<>();
        mVenuesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    Venue venue = dataSnapshot.getValue(Venue.class);
                    venues.add(new Venues(dataSnapshot.getKey(), venue.getName()));
                }
                ArrayAdapter<Venues> spinnerArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, venues);
                mVenueSpinner.setAdapter(spinnerArrayAdapter);
                int currentVenue = -1;
                if (mMexEntry != null) {
                    currentVenue = Venues.getPositionOfKey(venues, mMexEntry.getVenueKey());
                }
                mVenueSpinner.setSelection(currentVenue);
                mVenueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Venues v = (Venues) parent.getItemAtPosition(position);
                        mMexEntry.setVenueKey(v.getFirebaseKey());
                        mDetailDatabaseReference.setValue(mMexEntry);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    private void initializeMexEntryDetails() {
        mDetailDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntry = aDataSnapshot.getValue(MexEntry.class);
                if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
                    if (MiscUtils.LOLLIPOP_AND_HIGHER) {
                        mDetailImageView.setTransitionName(getString(R.string.shared_element_mex_entry_image_view));
                    }
                    Picasso.get().load(mMexEntry.getImageUrl()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mDetailImageView.setImageBitmap(bitmap);
                            if (MiscUtils.LOLLIPOP_AND_HIGHER) {
                                supportStartPostponedEnterTransition();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                }
                mDetailEditText.setText(mMexEntry.getName());
                mDetailPriceEditText.setText(String.valueOf(mMexEntry.getPrice()));
                mDetailRatingBar.setRating(mMexEntry.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    public void onAddNewPhotoMexEntry(View view) {
        dispatchCameraIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsUtils.sendScreenImageName(mTracker, DetailActivity.class.getSimpleName());
    }

    private void dispatchCameraIntent(int aPermissionRequestId) {
        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, aPermissionRequestId, this)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra("return-data", true);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, aPermissionRequestId);
                AnalyticsUtils.sendScreenImageName(mTracker, DetailActivity.class.getSimpleName() + "-take-picture-intent");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_IMAGE_CAPTURE_MEX_ENTRY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchCameraIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
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
            case RC_IMAGE_CAPTURE_MEX_ENTRY:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    Uri selectedImageUri = MiscUtils.getImageUri(this, bitmap);
                    mDetailImageView.setImageBitmap(bitmap);
                    if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
                        mFirebaseStorage.getReferenceFromUrl(mMexEntry.getImageUrl()).delete();
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
                                mMexEntry.setImageUrl(downloadUri.toString());
                                mDetailDatabaseReference.setValue(mMexEntry);
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
                break;
        }
    }

}
