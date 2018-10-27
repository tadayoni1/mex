package net.tirgan.mex.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import net.tirgan.mex.utilities.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class MexAdapter extends RecyclerView.Adapter<MexAdapter.MexAdapterViewHolder>
        implements Filterable {

    private final Context mContext;
    private final DatabaseReference mEntriesDatabaseReference;
    private List<Pair<MexEntry, String>> mMexEntryPairs;

    private final MexAdapterOnClickHandler mClickHandler;

    public interface MexAdapterOnClickHandler {
        void onMexClick(String aKey);
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

        mEntriesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntryPairs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    String key = dataSnapshot.getKey();
                    mMexEntryPairs.add(new Pair<>(mexEntry, key));
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
    public MexAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_mex, parent, false);
        view.setFocusable(true);

        return new MexAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MexAdapterViewHolder holder, int position) {
        final Pair<MexEntry, String> mexEntryPairs = mMexEntryPairs.get(position);

        if (mexEntryPairs.first.getImageUrl() != null && !mexEntryPairs.first.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(mexEntryPairs.first.getImageUrl())
                    .into(holder.mMexEntryImageView);
        } else {
            Picasso.get()
                    .load(FirebaseUtils.MEX_ENTRY_DEFAULT_IMAGE_DOWNLOAD_URL)
                    .into(holder.mMexEntryImageView);
        }
        holder.mMexEntryTextView.setText(mexEntryPairs.first.getName());
        holder.mMexEntryRatingBar.setRating(mexEntryPairs.first.getRating());

    }

    @Override
    public int getItemCount() {
        if (mMexEntryPairs == null) {
            return 0;
        } else {
            return mMexEntryPairs.size();
        }
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class MexAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMexEntryImageView;
        public final TextView mMexEntryTextView;
        public final RatingBar mMexEntryRatingBar;
        public final CardView mMexListItemCardView;

        public MexAdapterViewHolder(View itemView) {
            super(itemView);
            mMexEntryImageView = itemView.findViewById(R.id.mex_entry_lv_iv);
            mMexEntryTextView = itemView.findViewById(R.id.mex_entry_lv_tv);
            mMexEntryRatingBar = itemView.findViewById(R.id.mex_entry_lv_rb);
            mMexListItemCardView = itemView.findViewById(R.id.mex_list_item_cv);

            mMexEntryImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onMexClick(mMexEntryPairs.get(adapterPosition).second);
        }
    }
}
