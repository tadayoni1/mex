package net.tirgan.mex.ui.venue;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import net.tirgan.mex.R;
import net.tirgan.mex.model.Venue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VenueActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_FIREBASE_DATABASE_KEY = "intent-extra-firebase-database-key";

    @BindView(R.id.venue_iv)
    ImageView mVenueImageView;

    @BindView(R.id.venue_rb)
    RatingBar mRatingBar;

    @BindView(R.id.venue_et)
    EditText mEditText;

    private FirebaseStorage mFirebaseStorage;
    private FirebaseDatabase mVenuesDatabase;

    private StorageReference mVenuesStorageReference;
    private DatabaseReference mVenuesDatabaseReference;

    private Venue mVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        ButterKnife.bind(this);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mVenuesDatabase = FirebaseDatabase.getInstance();

        Intent intentThatStartedThisActivity = getIntent();
        String key = intentThatStartedThisActivity.getStringExtra(INTENT_EXTRA_FIREBASE_DATABASE_KEY);

//        mVenuesStorageReference = mFirebaseStorage.getReference().child("venue_photos");
        String userId = FirebaseAuth.getInstance().getUid();
        mVenuesDatabaseReference = mVenuesDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.venues_database)).child(key);
        initializeVenueDetails();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

    private void initializeVenueDetails() {
        mVenuesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mVenue = aDataSnapshot.getValue(Venue.class);
                Picasso.get().load(mVenue.getImageUri()).into(mVenueImageView);
                mEditText.setText(mVenue.getName());
                mRatingBar.setRating(mVenue.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

}
