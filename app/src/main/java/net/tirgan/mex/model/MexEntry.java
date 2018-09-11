package net.tirgan.mex.model;

public class MexEntry {

    private String mVenueKey;
    private String mName;
    private float mRating;
    private float mPrice;
    private String mImageUrl;
    private long mDate;

    public MexEntry() {
    }

    public MexEntry(String aVenueKey, String aName, float aRating, float aPrice, String aImageUrl, long aDate) {
        mVenueKey = aVenueKey;
        mName = aName;
        mRating = aRating;
        mPrice = aPrice;
        mImageUrl = aImageUrl;
        mDate = aDate;
    }

    public String getVenueKey() {
        return mVenueKey;
    }

    public void setVenueKey(String aVenueKey) {
        mVenueKey = aVenueKey;
    }

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
}
