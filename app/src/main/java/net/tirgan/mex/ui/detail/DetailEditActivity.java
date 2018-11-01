package net.tirgan.mex.ui.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
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
import net.tirgan.mex.model.MexLikelyPlaces;
import net.tirgan.mex.searchablespinner.SearchableSpinner;
import net.tirgan.mex.ui.main.MainActivity;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.MiscUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailEditActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String INSTANCE_STATE_MEX_ENTRY = "instance-state-mex-entry";

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;
    private static final int RC_LOCATION_MEX_ENTRY = 4;
    private static final int RC_LOCATION_CURRENT = 5;


    private DatabaseReference mDetailDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GoogleApiClient mGoogleApiClient;


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

    @BindView(R.id.detail_edit_place_spinner)
    SearchableSpinner mPlaceSpinner;

    @BindView(R.id.detail_pick_venue_pb)
    ProgressBar mDetailPickVenueProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);


        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

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

        initializePlaceSpinner();

        mDetailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCameraIntent(RC_IMAGE_CAPTURE_MEX_ENTRY);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (mMexEntry.getComment() == null || !mMexEntry.getComment().equals(mDetailEditText.getText().toString())) {
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
        mPlaceSpinner.onTouch();

//        dispatchPlacesIntent(RC_LOCATION_MEX_ENTRY);

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

            case RC_LOCATION_CURRENT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializePlaceSpinner();
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

    private void initializePlaceSpinner() {

        if (MiscUtils.checkPermissionsAndRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_LOCATION_CURRENT, this)) {

            mDetailPickVenueProgressBar.setVisibility(View.VISIBLE);
            mDetailPickVenueEditText.setText(getString(R.string.loading_nearby_places));

            final List<MexLikelyPlaces> likelyPlacesArray = new ArrayList<>();
            @SuppressLint("MissingPermission") Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    mDetailPickVenueEditText.setText("");
                    if (task.isSuccessful() && task.getResult() != null) {
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {

                            String placeId = placeLikelihood.getPlace().getId();
                            String placeName = placeLikelihood.getPlace().getName().toString();
                            likelyPlacesArray.add(new MexLikelyPlaces(placeId, placeName));
                        }
                        likelyPlaces.release();

                        ArrayAdapter<MexLikelyPlaces> likelyPlacesArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, likelyPlacesArray);
                        mPlaceSpinner.setAdapter(likelyPlacesArrayAdapter);

                        mPlaceSpinner.setPositiveButton(getString(R.string.find_on_map), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dispatchPlacesIntent(RC_LOCATION_MEX_ENTRY);
                            }
                        });
                        mPlaceSpinner.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        mPlaceSpinner.setTitle(getString(R.string.detail_pick_venue));
                        mPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                MexLikelyPlaces mexLikelyPlaces = (MexLikelyPlaces) parent.getItemAtPosition(position);
                                mMexEntry.setPlaceId(mexLikelyPlaces.getPlaceId());
                                mDetailPickVenueEditText.setText(mexLikelyPlaces.getPlaceName());
                                mDetailDatabaseReference.setValue(mMexEntry);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                    mDetailPickVenueProgressBar.setVisibility(View.GONE);
                }
            });
//            mDetailPickVenueProgressBar.setVisibility(View.VISIBLE);
//            mDetailPickVenueEditText.setText(getString(R.string.loading_nearby_places));
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(Places.PLACE_DETECTION_API).build();
//            @SuppressLint("MissingPermission") PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                    .getCurrentPlace(mGoogleApiClient, null);
//            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                @Override
//                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
//                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                        String placeId = placeLikelihood.getPlace().getId();
//                        String placeName = placeLikelihood.getPlace().getName().toString();
//                        likelyPlacesArray.add(new MexLikelyPlaces(placeId, placeName));
//                    }
//                    likelyPlaces.release();
//
//                    ArrayAdapter<MexLikelyPlaces> likelyPlacesArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, likelyPlacesArray);
//                    mPlaceSpinner.setAdapter(likelyPlacesArrayAdapter);
//
//                    mPlaceSpinner.setPositiveButton(getString(R.string.find_on_map), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dispatchPlacesIntent(RC_LOCATION_MEX_ENTRY);
//                        }
//                    });
//                    mPlaceSpinner.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    mPlaceSpinner.setTitle(getString(R.string.detail_pick_venue));
//                    mPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            MexLikelyPlaces mexLikelyPlaces = (MexLikelyPlaces) parent.getItemAtPosition(position);
//                            mMexEntry.setPlaceId(mexLikelyPlaces.getPlaceId());
//                            mDetailPickVenueEditText.setText(mexLikelyPlaces.getPlaceName());
//                            mDetailDatabaseReference.setValue(mMexEntry);
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> parent) {
//
//                        }
//                    });
//                    if(mPlaceSpinner.getChildCount() ==0) {
//                        mDetailPickVenueEditText.setText(getString(R.string.detail_pick_venue));
//                    }
//                    mDetailPickVenueProgressBar.setVisibility(View.GONE);
//                }
//            });
        }
    }


    @Override
    public void onConnected(@Nullable Bundle aBundle) {

    }

    @Override
    public void onConnectionSuspended(int aI) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult aConnectionResult) {

    }
}
