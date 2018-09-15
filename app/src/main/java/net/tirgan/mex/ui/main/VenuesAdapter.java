package net.tirgan.mex.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import net.tirgan.mex.model.Venue;
import net.tirgan.mex.utilities.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class VenuesAdapter
        extends RecyclerView.Adapter<VenuesAdapter.VenuesAdapterViewHolder>
        implements Filterable {


    public static final int SORT_BY_ENTRY_DATE = 1;
    // All new sort options must have a value greater than VenuesAdapter.SORT_BY_ENTRY_DATE
    public static final int SORT_BY_NAME = 2;
    public static final int SORT_BY_RATING = 3;

    private final Context mContext;
    private final FirebaseDatabase mDatabase;
    private final DatabaseReference mVenuesDatabaseReference;
    private List<Pair<Venue, String>> mVenuePairs;
    private List<Pair<Venue, String>> mVenuePairsFiltered;


    private int mSortBy;
    private float mFilterByMinRating;

    public void setSortAndFilter(int aSortBy, float aFilterByMinRating) {
        mSortBy = aSortBy;
        mFilterByMinRating = aFilterByMinRating;
    }

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

        mFilterByMinRating = 0;
        mSortBy = 1;

        mVenuesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                // TODO: disable enable searchview before and after data is loaded
                mVenuePairs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    Venue venue = dataSnapshot.getValue(Venue.class);
                    String key = dataSnapshot.getKey();
                    mVenuePairs.add(new Pair<>(venue, key));
                }
                mVenuePairsFiltered = mVenuePairs;
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    public void reloadData() {
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
        Pair<Venue, String> venuePair = mVenuePairsFiltered.get(position);
        if (venuePair.first.getImageUri() != null && !venuePair.first.getImageUri().isEmpty()) {
            Picasso.get()
                    .load(venuePair.first.getImageUri())
                    .into(holder.mVenueImageView);
        }
        holder.mVenueTextView.setText(venuePair.first.getName());
        holder.mVenueRatingBar.setRating(venuePair.first.getRating());

//        String userId = FirebaseAuth.getInstance().getUid();
//        DatabaseReference mexEntriesDatabaseReference = mDatabase.getReference()
//                .child(mContext.getString(R.string.users_database))
//                .child(userId)
//                .child(mContext.getString(R.string.entries_database));


        holder.mVenueRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.mVenueRecyclerView.setAdapter(new EntriesAdapter(mContext, mVenuePairsFiltered.get(position).second));

    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String constraintString = constraint.toString().toLowerCase();
                List<Pair<Venue, String>> venuePairs = new ArrayList<>(mVenuePairs);

                // Sort
                // if mSortBy equals to VenuesAdapter.SORT_BY_ENTRY_DATE then we don't need to sort.
                // All new sort options must have a value greater than VenuesAdapter.SORT_BY_ENTRY_DATE
                if (mSortBy > VenuesAdapter.SORT_BY_ENTRY_DATE) {
                    venuePairs = MiscUtils.sortVenues(venuePairs, mSortBy);
                }

                mVenuePairsFiltered = new ArrayList<>();
                for (int i = 0; i < venuePairs.size(); i++) {
                    // Filter
                    if (venuePairs.get(i).first.getRating() >= mFilterByMinRating) {
                        // Search
                        if (venuePairs.get(i).first.getName().toLowerCase().contains(constraintString)) {
                            mVenuePairsFiltered.add(venuePairs.get(i));
                        }
                    }
                }
                return null;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        }

                ;

    }


    @Override
    public int getItemCount() {
        if (mVenuePairsFiltered == null) {
            return 0;
        } else {
            return mVenuePairsFiltered.size();
        }
    }

    public class VenuesAdapterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView mVenueImageView;
        public final TextView mVenueTextView;
        public final RatingBar mVenueRatingBar;
        public final RecyclerView mVenueRecyclerView;
        public final ImageButton mVenueExpandImageButton;

        public VenuesAdapterViewHolder(View itemView) {
            super(itemView);
            mVenueImageView = itemView.findViewById(R.id.venue_lv_iv);
            mVenueTextView = itemView.findViewById(R.id.venue_lv_tv);
            mVenueRatingBar = itemView.findViewById(R.id.venue_lv_rb);
            mVenueRecyclerView = itemView.findViewById(R.id.venue_entries_lv);
            mVenueExpandImageButton = itemView.findViewById(R.id.expand_btn);

            mVenueExpandImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVenueRecyclerView.getVisibility() == View.VISIBLE) {
                        mVenueRecyclerView.setVisibility(View.GONE);
                    } else {
                        mVenueRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            });

            mVenueImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onVenueImageClick(mVenuePairsFiltered.get(adapterPosition).second);
        }
    }
}
