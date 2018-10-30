package net.tirgan.mex.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MexEntry implements Parcelable {

    private String mName;
    private float mRating;
    private float mPrice;
    private String mImageUrl;
    private long mDate;
    private String mPlaceId;

    public MexEntry() {
    }

    public MexEntry(String aName, float aRating, float aPrice, String aImageUrl, long aDate, String aPlaceId) {
        mName = aName;
        mRating = aRating;
        mPrice = aPrice;
        mImageUrl = aImageUrl;
        mDate = aDate;
        mPlaceId = aPlaceId;
    }

    protected MexEntry(Parcel in) {
        mName = in.readString();
        mRating = in.readFloat();
        mPrice = in.readFloat();
        mImageUrl = in.readString();
        mDate = in.readLong();
        mPlaceId = in.readString();
    }

    public static final Creator<MexEntry> CREATOR = new Creator<MexEntry>() {
        @Override
        public MexEntry createFromParcel(Parcel in) {
            return new MexEntry(in);
        }

        @Override
        public MexEntry[] newArray(int size) {
            return new MexEntry[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float aRating) {
        mRating = aRating;
    }

    public float getPrice() {
        return mPrice;
    }

    public void setPrice(float aPrice) {
        mPrice = aPrice;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String aImageUrl) {
        mImageUrl = aImageUrl;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long aDate) {
        mDate = aDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeFloat(mRating);
        dest.writeFloat(mPrice);
        dest.writeString(mImageUrl);
        dest.writeLong(mDate);
        dest.writeString(mPlaceId);
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String aPlaceId) {
        mPlaceId = aPlaceId;
    }
}
