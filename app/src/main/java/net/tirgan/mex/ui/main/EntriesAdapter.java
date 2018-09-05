package net.tirgan.mex.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
                Picasso.get().load(mexEntry.getImageUrl()).into(mMexEntryImageView);
                mMexEntryTextView.setText(mexEntry.getName());
                mMexEntryRatingBar.setRating(mexEntry.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });

        return convertView;
    }
}
