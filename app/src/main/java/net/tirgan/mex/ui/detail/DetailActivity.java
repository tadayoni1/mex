package net.tirgan.mex.ui.detail;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.tirgan.mex.MyFirebaseApp;
import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.ui.main.MainActivity;
import net.tirgan.mex.ui.settings.SettingsActivity;
import net.tirgan.mex.utilities.AnalyticsUtils;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.SettingsUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY = "intent-extra-detail-firebase-database-key";

    private static final int RC_IMAGE_CAPTURE_MEX_ENTRY = 3;
    private static final int RC_SHARE_EXTERNAL_STORAGE = 4;

    private static final String INSTANCE_STATE_MEX_ENTRY = "instance-state-mex-entry";

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int ACTIVITY_FOR_RESULT_DETAIL_EDIT = 10;

    @BindView(R.id.detail_thumbnail_iv)
    ImageView mDetailImageView;

    @BindView(R.id.detail_rb)
    RatingBar mDetailRatingBar;

    @BindView(R.id.mex_title_tv)
    TextView mDetailEditText;

    @BindView(R.id.mex_venue_date_tv)
    TextView mVenueDateTextView;

    @BindView(R.id.mex_price_tv)
    TextView mMexPriceTextView;

    @BindView(R.id.detail_comment_cv)
    CardView mDetailCommentCardView;

    @BindView(R.id.detail_comment_tv)
    TextView mDetailCommentTextView;

    private DatabaseReference mDetailDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;


    private MexEntry mMexEntry;
    private String mKey;
    private Tracker mTracker;

    private boolean mIsDefaultImageLoaded;
    private GeoDataClient mGeoDataClient;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MiscUtils.LOLLIPOP_AND_HIGHER && getResources().getBoolean(R.bool.is_animation_enabled)) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Fade fade = new Fade();
            fade.setDuration(1000);
            getWindow().setEnterTransition(fade);
        }
        setContentView(R.layout.activity_detail);

        if (MiscUtils.LOLLIPOP_AND_HIGHER && getResources().getBoolean(R.bool.is_animation_enabled)) {
            supportPostponeEnterTransition();
        }

        ButterKnife.bind(this);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        mDetailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsDefaultImageLoaded) {
                    Picasso.get()
                            .load(mMexEntry.getImageUrl())
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    final ImageView imageView = new ImageView(getApplicationContext());
                                    imageView.setImageBitmap(bitmap);
                                    Dialog dialog = new Dialog(DetailActivity.this);
                                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                                    dialog.getWindow().setBackgroundDrawable(
                                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                    dialog.addContentView(imageView, new RelativeLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));
                                    imageView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                                    imageView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                                    imageView.setAdjustViewBounds(true);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    dialog.show();
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                }
            }
        });


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

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
        menu.add(0, SettingsUtil.MENU_ITEM_EDIT, SettingsUtil.MENU_ITEM_EDIT, getString(R.string.menu_item_edit)).setIcon(android.R.drawable.ic_menu_edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, SettingsUtil.MENU_ITEM_SHARE, SettingsUtil.MENU_ITEM_SHARE, getString(R.string.menu_item_share)).setIcon(android.R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, SettingsUtil.MENU_ITEM_DELETE, SettingsUtil.MENU_ITEM_DELETE, getString(R.string.menu_item_delete)).setIcon(android.R.drawable.ic_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
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
                                if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(mMexEntry.getImageUrl()).delete();
                                }
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
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                break;
            case SettingsUtil.MENU_ITEM_SHARE:
                AnalyticsUtils.sendScreenImageName(mTracker, DetailActivity.class.getSimpleName() + "-share");
                share();
                break;
            case SettingsUtil.MENU_ITEM_EDIT:
                Intent intentDetailEdit = new Intent(DetailActivity.this, DetailEditActivity.class);
                intentDetailEdit.putExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY, mKey);
                startActivityForResult(intentDetailEdit, ACTIVITY_FOR_RESULT_DETAIL_EDIT);
                break;

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_SHARE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    share();
                } else {


                }
                break;
        }
    }

    private void share() {
        if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(mMexEntry.getImageUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Uri uri = MiscUtils.getImageUri(getApplicationContext(), bitmap);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            shareIntent.setDataAndType(uri, getContentResolver().getType(uri));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_FOR_RESULT_DETAIL_EDIT) {
//            initializeMexEntryDetails();
        }
    }


    private void initializeFirebase() {
        FirebaseDatabase detailDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        String userId = FirebaseAuth.getInstance().getUid();
        mDetailDatabaseReference = detailDatabase.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database)).child(mKey);

        mStorageReference = mFirebaseStorage.getReference().child(getString(R.string.users_database)).child(userId).child(getString(R.string.entries_database));
    }

    private void initializeMexEntryDetails() {
        mDetailDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntry = aDataSnapshot.getValue(MexEntry.class);
                if (mMexEntry != null) {
                    if (mMexEntry.getImageUrl() != null && !mMexEntry.getImageUrl().isEmpty()) {
                        mIsDefaultImageLoaded = false;
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
                        mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryWithTransparency));
                        mIsDefaultImageLoaded = true;
                        Picasso.get()
                                .load(FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL)
                                .into(mDetailImageView);
                    }
                    mDetailEditText.setText(mMexEntry.getName());
                    final String formattedDate = MiscUtils.getFormattedDate(mMexEntry.getDate());
                    if (mMexEntry.getPlaceId() != null && !mMexEntry.getPlaceId().isEmpty()) {
                        mGeoDataClient.getPlaceById(mMexEntry.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                if (task.isSuccessful()) {
                                    PlaceBufferResponse places = task.getResult();
                                    Place myPlace = places.get(0);
                                    mVenueDateTextView.setText(getString(R.string.format_venue_date, formattedDate, myPlace.getName()));
                                    places.release();
                                } else {
                                    mVenueDateTextView.setText(formattedDate);
                                    Log.e(TAG, "Place not found.");
                                }
                            }
                        });
                    } else {
                        mVenueDateTextView.setText(formattedDate);
                    }
                    mMexPriceTextView.setText(getString(R.string.format_price, String.valueOf(mMexEntry.getPrice())));
                    mDetailRatingBar.setRating(mMexEntry.getRating());
                    if (mMexEntry.getComment() == null || mMexEntry.getComment().isEmpty()) {
                        mDetailCommentCardView.setVisibility(View.GONE);
                    } else {
                        mDetailCommentCardView.setVisibility(View.VISIBLE);
                        mDetailCommentTextView.setText(mMexEntry.getComment());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsUtils.sendScreenImageName(mTracker, DetailActivity.class.getSimpleName());
    }


}
