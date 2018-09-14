package net.tirgan.mex.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.tirgan.mex.R;

public class ListFragment
        extends Fragment
        implements VenuesAdapter.VenuesAdapterOnClickHandler {


    private RecyclerView mRecyclerView;
    private VenuesAdapter mVenuesAdapter;
    private SearchView mSearchView;
    private ImageButton mSortByImageButton;

    private ListFragmentOnClickHandler mClickHandler;

    public interface ListFragmentOnClickHandler {
        void onVenueImageClick(String key);

        void onSortByImageButtonClick();
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

        mSearchView = rootView.findViewById(R.id.fragment_list_sv);
        mSortByImageButton = rootView.findViewById(R.id.sort_by_ib);

        mRecyclerView = rootView.findViewById(R.id.venues_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mVenuesAdapter = new VenuesAdapter(getContext(), this);
        mRecyclerView.setAdapter(mVenuesAdapter);
        mVenuesAdapter.reloadData();


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mVenuesAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mVenuesAdapter.getFilter().filter(newText);
                return false;
            }
        });

        mSortByImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onSortByImageButtonClick();
            }
        });

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
        mClickHandler.onVenueImageClick(key);
//        Intent intent = new Intent((Activity) mClickHandler, VenueActivity.class);
//        intent.putExtra(VenueActivity.INTENT_EXTRA_FIREBASE_DATABASE_KEY, key);
//        startActivity(intent);

    }

    public void reloadData() {
        if (mVenuesAdapter != null) {
            mVenuesAdapter.reloadData();
        }
    }

    public void setSortAndFilter(int aSortBy, float aFilterByMinRating) {
        mVenuesAdapter.setSortAndFilter(aSortBy, aFilterByMinRating);
        mVenuesAdapter.getFilter().filter(mSearchView.getQuery());
    }

}
