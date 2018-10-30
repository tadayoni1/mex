package net.tirgan.mex.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;
import net.tirgan.mex.utilities.FirebaseUtils;
import net.tirgan.mex.utilities.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class MexAdapter extends RecyclerView.Adapter<MexAdapter.MexAdapterViewHolder>
        implements Filterable {


    public static final int SORT_BY_ENTRY_DATE = 1;
    public static final int SORT_BY_NAME = 2;
    public static final int SORT_BY_RATING = 3;

    private int mSortBy;
    private float mFilterByMinRating;

    private final Context mContext;
    private final DatabaseReference mEntriesDatabaseReference;

    private List<Pair<MexEntry, String>> mMexEntryPairs;
    private List<Pair<MexEntry, String>> mMexEntryPairsFiltered;

    private final MexAdapterOnClickHandler mClickHandler;

    public interface MexAdapterOnClickHandler {
        void onMexClick(String aKey, View aView);
    }

    public MexAdapter(Context aContext, MexAdapterOnClickHandler aClickHandler) {
        mContext = aContext;
        mClickHandler = aClickHandler;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        mEntriesDatabaseReference = database.getReference()
                .child(mContext.getString(R.string.users_database))
                .child(userId)
                .child(mContext.getString(R.string.entries_database));

        mFilterByMinRating = 0;
        mSortBy = 1;


        mEntriesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntryPairs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    String key = dataSnapshot.getKey();
                    mMexEntryPairs.add(new Pair<>(mexEntry, key));
                }
                mMexEntryPairsFiltered = mMexEntryPairs;
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });

    }


    @NonNull
    @Override
    public MexAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_mex, parent, false);
        view.setFocusable(true);

        return new MexAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MexAdapterViewHolder holder, int position) {
        final Pair<MexEntry, String> mexEntryPairs = mMexEntryPairsFiltered.get(position);

        if (mexEntryPairs.first.getImageUrl() != null && !mexEntryPairs.first.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(mexEntryPairs.first.getImageUrl())
                    .into(holder.mMexEntryImageView);
        } else {
            Picasso.get()
                    .load(FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL)
                    .into(holder.mMexEntryImageView);
        }
        String name = mexEntryPairs.first.getName();
        if (name.length() > 18) {
            name = name.substring(0, 18) + "...";
        }
        holder.mMexEntryTextView.setText(name);
        holder.mMexEntryRatingTextView.setText(mContext.getString(R.string.format_rating_text_view, String.valueOf(mexEntryPairs.first.getRating())));

        holder.mMexEntryDateTextView.setText(MiscUtils.getFormattedDate(mexEntryPairs.first.getDate()));

//        int randomColor = ColorGenerator.MATERIAL.getRandomColor();
//        int randomColorWithTransparency = Color.argb(210, Color.red(randomColor), Color.green(randomColor), Color.blue(randomColor));
//        holder.mMexListItemCardView.setCardBackgroundColor(randomColorWithTransparency);

        if (mexEntryPairs.first.getPlaceId() != null && !mexEntryPairs.first.getPlaceId().isEmpty()) {
            GeoDataClient geoDataClient = Places.getGeoDataClient(mContext, null);
            geoDataClient.getPlaceById(mexEntryPairs.first.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        holder.mMexEntryVenueTextView.setText(myPlace.getName());
                        places.release();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if (mMexEntryPairsFiltered == null) {
            return 0;
        } else {
            return mMexEntryPairsFiltered.size();
        }
    }

    public void setSortAndFilter(int aSortBy, float aFilterByMinRating) {
        mSortBy = aSortBy;
        mFilterByMinRating = aFilterByMinRating;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String constraintString = constraint.toString().toLowerCase();
                List<Pair<MexEntry, String>> mexEntryPairs = new ArrayList<>(mMexEntryPairs);

                // Sort
                // if mSortBy equals to MexAdapter.SORT_BY_ENTRY_DATE then we don't need to sort.
                // All new sort options must have a value greater than MexAdapter.SORT_BY_ENTRY_DATE
                if (mSortBy > MexAdapter.SORT_BY_ENTRY_DATE) {
                    mexEntryPairs = MiscUtils.sortMexEntries(mexEntryPairs, mSortBy);
                }

                mMexEntryPairsFiltered = new ArrayList<>();
                for (int i = 0; i < mexEntryPairs.size(); i++) {
                    // Filter
                    if (mexEntryPairs.get(i).first.getRating() >= mFilterByMinRating) {
                        // Search
                        if (mexEntryPairs.get(i).first.getName().toLowerCase().contains(constraintString)) {
                            mMexEntryPairsFiltered.add(mexEntryPairs.get(i));
                        }
                    }
                }
                return null;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };

    }

    public class MexAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMexEntryImageView;
        public final TextView mMexEntryTextView;
        public final TextView mMexEntryVenueTextView;
        public final TextView mMexEntryRatingTextView;
        public final TextView mMexEntryDateTextView;


        public MexAdapterViewHolder(View itemView) {
            super(itemView);
            mMexEntryImageView = itemView.findViewById(R.id.mex_entry_lv_iv);
            mMexEntryTextView = itemView.findViewById(R.id.mex_entry_lv_tv);
            mMexEntryVenueTextView = itemView.findViewById(R.id.mex_venue_lv_tv);
            mMexEntryRatingTextView = itemView.findViewById(R.id.mex_entry_rating_tv);
            mMexEntryDateTextView = itemView.findViewById(R.id.mex_entry_date);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            // if entry has a picture then pass v to open detail activity with an animation, otherwise pass null to skip animation
            if (mMexEntryPairsFiltered.get(adapterPosition).first.getImageUrl() != null && !mMexEntryPairsFiltered.get(adapterPosition).first.getImageUrl().isEmpty()) {
                mClickHandler.onMexClick(mMexEntryPairsFiltered.get(adapterPosition).second, v);
            } else {
                mClickHandler.onMexClick(mMexEntryPairsFiltered.get(adapterPosition).second, null);
            }
        }
    }
}
