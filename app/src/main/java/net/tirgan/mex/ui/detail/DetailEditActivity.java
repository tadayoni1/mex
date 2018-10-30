package net.tirgan.mex.ui.detail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import net.tirgan.mex.ui.main.MainActivity;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.MiscUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailEditActivity extends AppCompatActivity {

    private static final String INSTANCE_STATE_MEX_ENTRY = "instance-state-mex-entry";

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;
    private static final int RC_LOCATION_MEX_ENTRY = 4;


    private DatabaseReference mDetailDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private GeoDataClient mGeoDataClient;


    private MexEntry mMexEntry;
    private String mKey;
    private Tracker mTracker;

    @BindView(R.id.detail_edit_iv)
    ImageView mDetailImageView;

    @BindView(R.id.detail_et)
    EditText mDetailEditText;

    @BindView(R.id.detail_price_et)
    EditText mDetailPriceEditText;

    @BindView(R.id.detail_rb)
    RatingBar mDetailRatingBar;

    @BindView(R.id.detail_pick_venue_et)
    EditText mDetailPickVenueEditText;

    @BindView(R.id.detail_edit_comment_et)
    EditText mDetailEditCommentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_edit);


        ButterKnife.bind(this);


        mGeoDataClient = Places.getGeoDataClient(this, null);

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
        mKey = intentThatStartedThisActivity.getStringExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY);

        initializeFirebase();

        initializeMexEntryDetails();

        setChangeListeners();

        mDetailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCameraIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
            }
        });

    }

    private void setChangeListeners() {
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

        mDetailEditCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mMexEntry.setComment(mDetailEditCommentEditText.getText().toString());
                    mDetailDatabaseReference.setValue(mMexEntry);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (!mMexEntry.getComment().equals(mDetailEditText.getText())) {
            mMexEntry.setComment(mDetailEditCommentEditText.getText().toString());
            mDetailDatabaseReference.setValue(mMexEntry);
        }
        super.onStop();
    }

    public void onUpdateLocationClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //TODO: handle denied location access
            return;
        }
        dispatchPlacesIntent(RC_LOCATION_MEX_ENTRY);

    }

    private void dispatchPlacesIntent(int aPermissionRequestId) {
        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, aPermissionRequestId, this)) {
            try {
                // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
                // when a place is selected or with the user cancels.
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent i = builder.build(this);
                startActivityForResult(i, PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
            String placeName = place.getName().toString();
            String placeID = place.getId();

            mMexEntry.setPlaceId(placeID);
            mDetailDatabaseReference.setValue(mMexEntry);

            mDetailPickVenueEditText.setText(placeName);
        }
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            Uri selectedImageUri = Uri.fromFile(new File(image.getPath()));
            mDetailImageView.setImageURI(selectedImageUri);
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

    }

    private void dispatchCameraIntent(int aPermissionRequestId) {
        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, aPermissionRequestId, this)) {
            ImagePicker.create(this) // Activity or Fragment
                    .returnMode(ReturnMode.ALL)
                    .single()
                    .start();
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
            }
            case RC_LOCATION_MEX_ENTRY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchPlacesIntent(RC_LOCATION_MEX_ENTRY);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INSTANCE_STATE_MEX_ENTRY, mMexEntry);
    }


    private void initializeFirebase() {
        FirebaseDatabase detailDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        String userId = FirebaseAuth.getInstance().getUid();
        mDetailDatabaseReference = detailDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database)).child(mKey);

        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database));
    }

    private void initializeMexEntryDetails() {
        mDetailDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntry = aDataSnapshot.getValue(MexEntry.class);
                if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
                    if (MiscUtils.LOLLIPOP_AND_HIGHER && getResources().getBoolean(R.bool.is_animation_enabled)) {
                        mDetailImageView.setTransitionName(getString(R.string.shared_element_mex_entry_image_view));
                    }
                    final Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mDetailImageView.setImageBitmap(bitmap);
                            if (MiscUtils.LOLLIPOP_AND_HIGHER && getResources().getBoolean(R.bool.is_animation_enabled)) {
                                supportStartPostponedEnterTransition();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            if (MiscUtils.LOLLIPOP_AND_HIGHER && getResources().getBoolean(R.bool.is_animation_enabled)) {
                                supportStartPostponedEnterTransition();
                            }
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    };
                    Picasso.get().load(mMexEntry.getImageUrl()).into(target);
                    mDetailImageView.setTag(target);
                } else {
                    Picasso.get()
                            .load(FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL)
                            .into(mDetailImageView);
                }
                mDetailEditText.setText(mMexEntry.getName());
                if (mMexEntry.getPlaceId() != null && !mMexEntry.getPlaceId().isEmpty()) {
                    mGeoDataClient.getPlaceById(mMexEntry.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            if (task.isSuccessful()) {
                                PlaceBufferResponse places = task.getResult();
                                Place myPlace = places.get(0);
                                mDetailPickVenueEditText.setText(myPlace.getName());
                                places.release();
                            }
                        }
                    });
                }
                mDetailPriceEditText.setText(String.valueOf(mMexEntry.getPrice()));
                mDetailRatingBar.setRating(mMexEntry.getRating());

                mDetailEditCommentEditText.setText(mMexEntry.getComment());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

}
