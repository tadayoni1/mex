package net.tirgan.mex.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.tirgan.mex.R;


public class MexLikelyPlacesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private MexLikelyPlacesAdapter mMexLikelyPlacesAdapter;
    private Context mContext;

    public MexLikelyPlacesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_mex_likely_places, container, false);

        mRecyclerView = rootView.findViewById(R.id.mex_likely_places_rv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mMexLikelyPlacesAdapter = new MexLikelyPlacesAdapter(getContext());
        mRecyclerView.setAdapter(mMexLikelyPlacesAdapter);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mContext = context;
        } catch (ClassCastException aE) {
            throw new ClassCastException(context.toString()
                    + " must implement MexLikelyPlacesFragmentOnClickHandler");
        }

    }

}
