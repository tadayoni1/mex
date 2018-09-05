package net.tirgan.mex.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
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
import net.tirgan.mex.model.Venue;

import java.util.ArrayList;

public class VenuesAdapter
        extends RecyclerView.Adapter<VenuesAdapter.VenuesAdapterViewHolder> {


    private final Context mContext;
    private final FirebaseDatabase mDatabase;
    private final DatabaseReference mVenuesDatabaseReference;
    private ArrayList<Venue> mVenues;
    private ArrayList<String> mKeys;

    private final VenuesAdapterOnClickHandler mClickHandler;

    public interface VenuesAdapterOnClickHandler {
        void onVenueImageClick(String key);
    }


    public VenuesAdapter(Context aContext, VenuesAdapterOnClickHandler aClickHandler) {
        mContext = aContext;
        mDatabase = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        mVenuesDatabaseReference = mDatabase.getReference()
                .child(mContext.getString(R.string.users_database))
                .child(userId)
                .child(mContext.getString(R.string.venues_database));
        reloadData();
        mClickHandler = aClickHandler;
    }

    public void reloadData() {
        mVenuesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mVenues = new ArrayList<>();
                mKeys = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    Venue venue = dataSnapshot.getValue(Venue.class);
                    mVenues.add(venue);
                    String key = dataSnapshot.getKey();
                    mKeys.add(key);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    @NonNull
    @Override
    public VenuesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_venues, parent, false);
        view.setFocusable(true);

        return new VenuesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VenuesAdapterViewHolder holder, final int position) {
        Venue venue = mVenues.get(position);
        Picasso.get().load(venue.getImageUri()).into(holder.mVenueImageView);
        holder.mVenueTextView.setText(venue.getName());
        holder.mVenueRatingBar.setRating(venue.getRating());

        String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference mexEntriesDatabaseReference = mDatabase.getReference()
                .child(mContext.getString(R.string.users_database))
                .child(userId)
                .child(mContext.getString(R.string.entries_database));

        mexEntriesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                ArrayList<String> mexEntriesKeys = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    if (mexEntry.getVenueKey().equals(mKeys.get(position))) {
                        mexEntriesKeys.add(dataSnapshot.getKey());
                    }
                }
                if (mexEntriesKeys != null) {
                    holder.mVenueGridView.setAdapter(new EntriesAdapter(mContext, mexEntriesKeys, mKeys.get(position)));
//                    holder.mVenueGridView.setVisibility(View.VISIBLE);
                } else {
                    holder.mVenueGridView.setAdapter(null);
//                    holder.mVenueGridView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        if (mVenues == null) {
            return 0;
        } else {
            return mVenues.size();
        }
    }

    public class VenuesAdapterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mVenueImageView;
        public final TextView mVenueTextView;
        public final RatingBar mVenueRatingBar;
        public final GridView mVenueGridView;
        public final ImageButton mVenueExpandImageButton;

        public VenuesAdapterViewHolder(View itemView) {
            super(itemView);
            mVenueImageView = itemView.findViewById(R.id.venue_lv_iv);
            mVenueTextView = itemView.findViewById(R.id.venue_lv_tv);
            mVenueRatingBar = itemView.findViewById(R.id.venue_lv_rb);
            mVenueGridView = itemView.findViewById(R.id.venue_entries_gv);
            mVenueExpandImageButton = itemView.findViewById(R.id.expand_btn);

            mVenueExpandImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVenueGridView.getVisibility() == View.VISIBLE) {
                        mVenueGridView.setVisibility(View.GONE);
                    } else {
                        mVenueGridView.setVisibility(View.VISIBLE);
                    }
                }
            });

            mVenueImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onVenueImageClick(mKeys.get(adapterPosition));
        }
    }
}
