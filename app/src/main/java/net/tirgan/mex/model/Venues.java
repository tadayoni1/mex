package net.tirgan.mex.model;

import java.util.List;

public class Venues {

    private final String mFirebaseKey;
    private final String mVenueName;

    public Venues(String aFirebaseKey, String aVenueName) {
        mFirebaseKey = aFirebaseKey;
        mVenueName = aVenueName;
    }

    public String getFirebaseKey() {
        return mFirebaseKey;
    }

    public String getVenueName() {
        return mVenueName;
    }

    public static int getPositionOfKey(List<Venues> aVenues, String aFirebaseKey) {
        for (int i = 0; i < aVenues.size(); i++) {
            if (aVenues.get(i).getFirebaseKey().equals(aFirebaseKey)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public String toString() {
        return mVenueName;
    }
}
