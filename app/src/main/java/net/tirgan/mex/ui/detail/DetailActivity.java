package net.tirgan.mex.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.squareup.picasso.Picasso;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY = "intent-extra-detail-firebase-database-key";

    @BindView(R.id.detail_iv)
    ImageView mDetailImageView;

    @BindView(R.id.detail_rb)
    RatingBar mDetailRatingBar;

    @BindView(R.id.detail_et)
    EditText mDetailEditText;

    private FirebaseDatabase mDetailDatabase;

    private DatabaseReference mDetailDatabaseReference;

    private MexEntry mMexEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        mDetailDatabase = FirebaseDatabase.getInstance();

        Intent intentThatStartedThisActivity = getIntent();
        String key = intentThatStartedThisActivity.getStringExtra(INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY);

        String userId = FirebaseAuth.getInstance().getUid();
        mDetailDatabaseReference = mDetailDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database)).child(key);
        initializeMexEntryDetails();

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

    }

    private void initializeMexEntryDetails() {
        mDetailDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntry = aDataSnapshot.getValue(MexEntry.class);
                Picasso.get().load(mMexEntry.getImageUrl()).into(mDetailImageView);
                mDetailEditText.setText(mMexEntry.getName());
                mDetailRatingBar.setRating(mMexEntry.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

}
