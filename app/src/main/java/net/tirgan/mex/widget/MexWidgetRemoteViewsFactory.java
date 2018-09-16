package net.tirgan.mex.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexEntry;

import java.util.ArrayList;
import java.util.List;

class MexWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private List<MexEntry> mMexEntries;
    private final String mVenueKey;
    private final int mAppWidgetId;

    public MexWidgetRemoteViewsFactory(final Context aContext, Intent aIntent) {
        mContext = aContext;
        mVenueKey = aIntent.getStringExtra(MexWidgetProvider.WIDGET_INTENT_EXTRA_VENUE_KEY);
        mAppWidgetId = aIntent.getIntExtra(MexWidgetProvider.WIDGET_INTENT_EXTRA_APPWIDGET_ID, -1);
        String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.users_database))
                .child(userId)
                .child(mContext.getString(R.string.entries_database));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot aDataSnapshot) {
                mMexEntries = new ArrayList<>();
                for (DataSnapshot dataSnapshot : aDataSnapshot.getChildren()) {
                    MexEntry mexEntry = dataSnapshot.getValue(MexEntry.class);
                    if (mexEntry.getVenueKey().equals(mVenueKey)) {
                        mMexEntries.add(mexEntry);
                    }
                }
                AppWidgetManager.getInstance(mContext).notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.ap_mex_lv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError aDatabaseError) {

            }
        });
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mMexEntries == null) {
            return 0;
        } else {
            return mMexEntries.size();
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || mMexEntries == null) {
            return null;
        }
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget_mex_entries);
        final String listText = mMexEntries.get(position).getName();
        final String priceText = String.format(mContext.getString(R.string.format_price), String.valueOf(mMexEntries.get(position).getPrice()));
        rv.setTextViewText(R.id.ap_mex_li_name_tv, listText);
        rv.setTextViewText(R.id.ap_mex_li_price_tv, priceText);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
