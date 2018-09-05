package net.tirgan.mex.model;

public class Venues {

    private String mFirebaseKey;
    private String mVenueName;

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

    @Override
    public String toString() {
        return mVenueName;
    }
}
