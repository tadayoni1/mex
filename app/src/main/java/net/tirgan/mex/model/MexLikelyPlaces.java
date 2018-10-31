package net.tirgan.mex.model;

import java.util.List;

public class MexLikelyPlaces {

    private String mPlaceId;
    private String mPlaceName;

    public MexLikelyPlaces(String aPlaceId, String aPlaceName) {
        mPlaceId = aPlaceId;
        mPlaceName = aPlaceName;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String aPlaceId) {
        mPlaceId = aPlaceId;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public void setPlaceName(String aPlaceName) {
        mPlaceName = aPlaceName;
    }

    public static int getPositionOfKey(List<MexLikelyPlaces> aMexLikelyPlaces, String aPlaceId) {
        for (int i = 0; i < aMexLikelyPlaces.size(); i++) {
            if (aMexLikelyPlaces.get(i).getPlaceId().equals(aPlaceId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return mPlaceName;
    }
}
