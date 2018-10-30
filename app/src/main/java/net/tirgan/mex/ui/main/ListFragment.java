package net.tirgan.mex.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.tirgan.mex.R;
import net.tirgan.mex.utilities.SettingsUtil;

public class ListFragment
        extends Fragment
        implements  MexAdapter.MexAdapterOnClickHandler {


    private RecyclerView mRecyclerView;
    private MexAdapter mMexAdapter;
    private SearchView mSearchView;
//    private FloatingActionsMenu mFloatingActionsMenu;

    private ListFragmentOnClickHandler mClickHandler;

    @Override
    public void onMexClick(String aKey, View aView) {
        mClickHandler.onMexClick(aKey, aView);
    }


    public interface ListFragmentOnClickHandler {

        void onSortByImageButtonClick();

        void onMexClick(String aKey, View aView);
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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isAdEnabled = sp.getBoolean(SettingsUtil.PREF_AD_ENABLED, getContext().getResources().getBoolean(R.bool.ad_enabled_default));
        if (isAdEnabled) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }

//        mFloatingActionsMenu = rootView.findViewById(R.id.fragment_list_fam);

        mSearchView = rootView.findViewById(R.id.fragment_list_sv);
        ImageButton sortByImageButton = rootView.findViewById(R.id.sort_by_ib);

        mRecyclerView = rootView.findViewById(R.id.mex_rv);

        // TODO: span count
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mMexAdapter = new MexAdapter(getContext(), this);
        mRecyclerView.setAdapter(mMexAdapter);


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mMexAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mMexAdapter.getFilter().filter(newText);
                return false;
            }
        });

        sortByImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // opens sort/filter fragment
                mClickHandler.onSortByImageButtonClick();
            }
        });

        return rootView;
    }


    public void setSortAndFilter(int aSortBy, float aFilterByMinRating) {
        mMexAdapter.setSortAndFilter(aSortBy, aFilterByMinRating);
        mMexAdapter.getFilter().filter(mSearchView.getQuery());
    }

//    public void collapseFloatingActionMenu() {
//        mFloatingActionsMenu.collapse();
//    }

}
