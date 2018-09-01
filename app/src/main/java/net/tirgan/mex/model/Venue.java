package net.tirgan.mex.model;

public class Venue {

    private String mName;
    private String mImageUri;
    private float mRating;
    private double lat;
    private double lon;

    public Venue() {
    }

    public Venue(String aName, String aImageUri, float aRating, double aLat, double aLon) {
        mName = aName;
        mImageUri = aImageUri;
        mRating = aRating;
        lat = aLat;
        lon = aLon;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String aImageUri) {
        mImageUri = aImageUri;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float aRating) {
        mRating = aRating;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double aLat) {
        lat = aLat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double aLon) {
        lon = aLon;
    }
}
