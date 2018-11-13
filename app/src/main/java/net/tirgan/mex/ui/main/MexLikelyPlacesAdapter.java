package net.tirgan.mex.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import net.tirgan.mex.R;
import net.tirgan.mex.model.MexLikelyPlaces;
import net.tirgan.mex.utilities.MiscUtils;
import net.tirgan.mex.utilities.PlacesUtil;

import java.util.ArrayList;
import java.util.List;

public class MexLikelyPlacesAdapter extends RecyclerView.Adapter<MexLikelyPlacesAdapter.MexLikelyPlacesAdapterViewHolder> {

    private final Context mContext;
    private PlaceDetectionClient mPlaceDetectionClient;
    private List<MexLikelyPlaces> mLikelyPlacesArray;

    public MexLikelyPlacesAdapter(Context aContext) {
        mContext = aContext;
        reloadData();
    }

    public void reloadData() {
        mPlaceDetectionClient = Places.getPlaceDetectionClient(mContext, null);
        if (MiscUtils.checkPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, mContext)) {

            @SuppressLint("MissingPermission") Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLikelyPlacesArray = new ArrayList<>();
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            if (PlacesUtil.isFoodPlace(placeLikelihood.getPlace().getPlaceTypes())) {
                                String placeId = placeLikelihood.getPlace().getId();
                                String placeName = placeLikelihood.getPlace().getName().toString();
                                mLikelyPlacesArray.add(new MexLikelyPlaces(placeId, placeName));
                            }
                        }
                        likelyPlaces.release();
                        notifyDataSetChanged();
                    }
                }
            });
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.location_required), Toast.LENGTH_SHORT).show();
        }
    }


    @NonNull
    @Override
    public MexLikelyPlacesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_mex_likely_places, parent, false);
        view.setFocusable(true);

        return new MexLikelyPlacesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MexLikelyPlacesAdapterViewHolder holder, int position) {
        final MexLikelyPlaces mexLikelyPlaces = mLikelyPlacesArray.get(position);

        holder.mPlaceNameTextView.setText(mexLikelyPlaces.getPlaceName());
    }

    @Override
    public int getItemCount() {
        if (mLikelyPlacesArray == null) {
            return 0;
        } else {
            return mLikelyPlacesArray.size();
        }
    }

    public class MexLikelyPlacesAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mPlaceNameTextView;

        public MexLikelyPlacesAdapterViewHolder(View itemView) {
            super(itemView);
            mPlaceNameTextView = itemView.findViewById(R.id.place_name_tv);
        }
    }
}
