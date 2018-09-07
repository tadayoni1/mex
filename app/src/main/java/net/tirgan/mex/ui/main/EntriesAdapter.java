package net.tirgan.mex.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.ui.detail.DetailActivity;

import java.util.List;

public class EntriesAdapter extends ArrayAdapter<String> {


    private Context mContext;
    private String mVenueKey;


    public EntriesAdapter(@NonNull Context context, @NonNull List<String> keys, String aVenueKey) {
        super(context, 0, keys);
        mContext = context;
        mVenueKey = aVenueKey;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final String mexKey = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_mex_entries, parent, false);
        }

        final ImageView mMexEntryImageView = convertView.findViewById(R.id.mex_entry_lv_iv);
        final TextView mMexEntryTextView = convertView.findViewById(R.id.mex_entry_lv_tv);
        final RatingBar mMexEntryRatingBar = convertView.findViewById(R.id.mex_entry_lv_rb);
        final CardView mMexListItemCardView = convertView.findViewById(R.id.mex_list_item_cv);

        mMexEntryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY, mexKey);
                mContext.startActivity(intent);
            }
        });

        String userId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase mexEntryDatabase = FirebaseDatabase.getInstance();

        DatabaseReference mexEntryDatabaseReference = mexEntryDatabase.getReference()
                .child(getContext().getString(R.string.users_database))
                .child(userId)
                .child(getContext().getString(R.string.entries_database))
                .child(mexKey);

        mexEntryDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                MexEntry mexEntry = aDataSnapshot.getValue(MexEntry.class);
                if (mexEntry != null && mexEntry.getImageUrl() != null && !mexEntry.getImageUrl().isEmpty()) {
                    Picasso.get().load(mexEntry.getImageUrl()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mMexEntryImageView.setImageBitmap(bitmap);
                            if (bitmap != null) {
                                Palette p = Palette.from(bitmap).generate();
                                int color = p.getLightVibrantColor(0xFFFFFFFF);
                                mMexListItemCardView.setBackgroundColor(color);
                            }
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            mMexEntryImageView.setImageResource(R.drawable.noodle);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            mMexEntryImageView.setImageDrawable(placeHolderDrawable);
                        }
                    });
                    mMexEntryTextView.setText(mexEntry.getName());
                    mMexEntryRatingBar.setRating(mexEntry.getRating());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });

        return convertView;
    }
}
