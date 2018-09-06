package net.tirgan.mex.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.tirgan.mex.R;
import net.tirgan.mex.ui.venue.VenueActivity;

public class ListFragment
        extends Fragment
        implements VenuesAdapter.VenuesAdapterOnClickHandler {


    private RecyclerView mRecyclerView;
    private VenuesAdapter mVenuesAdapter;

    private ListFragmentOnClickHandler mClickHandler;

    public interface ListFragmentOnClickHandler {
        void onVenueImageClick(String key);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mClickHandler = (ListFragmentOnClickHandler) context;
        } catch (ClassCastException aE) {
            throw new ClassCastException(context.toString()
                    + " must implement ListFragmentOnClickHandler");
        }
    }

    public ListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        AdView adView = rootView.findViewById(R.id.list_av);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mRecyclerView = rootView.findViewById(R.id.venues_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mVenuesAdapter = new VenuesAdapter(getContext(), this);
        mRecyclerView.setAdapter(mVenuesAdapter);
        mVenuesAdapter.reloadData();


        return rootView;
    }

    private void initializeRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mVenuesAdapter = new VenuesAdapter(getContext(), this);
        mRecyclerView.setAdapter(mVenuesAdapter);
        mVenuesAdapter.reloadData();
    }

    @Override
    public void onVenueImageClick(String key) {
//        mClickHandler.onVenueImageClick(key);
        Intent intent = new Intent((Activity) mClickHandler, VenueActivity.class);
        intent.putExtra(VenueActivity.INTENT_EXTRA_FIREBASE_DATABASE_KEY, key);
        startActivity(intent);

    }

    public void reloadData() {
        if (mVenuesAdapter != null) {
            mVenuesAdapter.reloadData();
        }
    }
}
