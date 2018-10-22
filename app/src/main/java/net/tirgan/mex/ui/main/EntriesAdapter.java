package net.tirgan.mex.ui.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.ui.detail.DetailActivity;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.EntriesAdapterViewHolder> {


    private final Context mContext;
    private final String mVenueKey;
    private final DatabaseReference mEntriesDatabaseReference;
    private List<MexEntry> mMexEntries;
    private List<String> mKeys;


    @NonNull
    @Override
    public EntriesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_mex_entries, parent, false);
        view.setFocusable(true);

        return new EntriesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EntriesAdapterViewHolder holder, int position) {
        final MexEntry mexEntry = mMexEntries.get(position);

        if (mexEntry.getImageUrl() != null && !mexEntry.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(mexEntry.getImageUrl())
                    .into(holder.mMexEntryImageView);
        } else {
            Picasso.get()
                    .load(FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL)
                    .into(holder.mMexEntryImageView);
        }
        holder.mMexEntryTextView.setText(mexEntry.getName());
        holder.mMexEntryRatingBar.setRating(mexEntry.getRating());

        holder.mMexEntryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra(DetailActivity.INTENT_EXTRA_DETAIL_FIREBASE_DATABASE_KEY, mKeys.get(holder.getAdapterPosition()));
                if (MiscUtils.LOLLIPOP_AND_HIGHER && mContext.getResources().getBoolean(R.bool.is_animation_enabled)) {
                    v.setTransitionName(mContext.getString(R.string.shared_element_mex_entry_image_view));
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, v, mContext.getString(R.string.shared_element_mex_entry_image_view)).toBundle();
                    mContext.startActivity(intent, bundle);
                } else {
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mMexEntries == null) {
            return 0;
        } else {
            return mMexEntries.size();
        }
    }


    public EntriesAdapter(@NonNull Context aContext, @NonNull String aVenueKey) {
        mContext = aContext;
        mVenueKey = aVenueKey;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        mEntriesDatabaseReference = database.getReference()
                .child(mContext.getString(R.string.users_database))
                .child(userId)
                .child(mContext.getString(R.string.entries_database));
        reloadData();
    }

    public void reloadData() {
        mEntriesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntries = new ArrayList<>();
                mKeys = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    if (mexEntry.getVenueKey().equals(mVenueKey)) {
                        mMexEntries.add(mexEntry);
                        mKeys.add(dataSnapshot.getKey());
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    public class EntriesAdapterViewHolder extends RecyclerView.ViewHolder {

        public final ImageView mMexEntryImageView;
        public final TextView mMexEntryTextView;
        public final RatingBar mMexEntryRatingBar;
        public final CardView mMexListItemCardView;

        public EntriesAdapterViewHolder(View itemView) {
            super(itemView);
            mMexEntryImageView = itemView.findViewById(R.id.mex_entry_lv_iv);
            mMexEntryTextView = itemView.findViewById(R.id.mex_entry_lv_tv);
            mMexEntryRatingBar = itemView.findViewById(R.id.mex_entry_lv_rb);
            mMexListItemCardView = itemView.findViewById(R.id.mex_list_item_cv);
        }
    }
}
