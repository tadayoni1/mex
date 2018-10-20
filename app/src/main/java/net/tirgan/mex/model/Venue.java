package net.tirgan.mex.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Venue implements Parcelable{

    private String mName;
    private String mImageUrl;
    private float mRating;
    private double lat;
    private double lon;

    public Venue() {
    }

    public Venue(String aName, String aImageUri, float aRating, double aLat, double aLon) {
        mName = aName;
        mImageUrl = aImageUri;
        mRating = aRating;
        lat = aLat;
        lon = aLon;
    }

    protected Venue(Parcel in) {
        mName = in.readString();
        mImageUrl = in.readString();
        mRating = in.readFloat();
        lat = in.readDouble();
        lon = in.readDouble();
    }

    public static final Creator<Venue> CREATOR = new Creator<Venue>() {
        @Override
        public Venue createFromParcel(Parcel in) {
            return new Venue(in);
        }

        @Override
        public Venue[] newArray(int size) {
            return new Venue[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String aImageUrl) {
        mImageUrl = aImageUrl;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mImageUrl);
        dest.writeFloat(mRating);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }
}
