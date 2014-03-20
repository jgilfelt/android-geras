package com.readystatesoftware.android.geras.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jgilfelt on 20/03/2014.
 */
public class GerasLocationMonitor implements Parcelable {

    private String mSeries;
    private String mProvider;
    private long mMinTime;
    private float mMinDistance;

    public GerasLocationMonitor(String series, String provider, long minTime, float minDistance) {
        this.mSeries = series;
        this.mProvider = provider;
        this.mMinTime = minTime;
        this.mMinDistance = minDistance;
    }

    public String getSeries() {
        return mSeries;
    }

    public String getProvider() {
        return mProvider;
    }

    public long getMinTime() {
        return mMinTime;
    }

    public float getMinDistance() {
        return mMinDistance;
    }

    protected GerasLocationMonitor(Parcel in) {
        mSeries = in.readString();
        mProvider = in.readString();
        mMinTime = in.readLong();
        mMinDistance = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSeries);
        dest.writeString(mProvider);
        dest.writeLong(mMinTime);
        dest.writeFloat(mMinDistance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GerasLocationMonitor> CREATOR = new Parcelable.Creator<GerasLocationMonitor>() {
        @Override
        public GerasLocationMonitor createFromParcel(Parcel in) {
            return new GerasLocationMonitor(in);
        }

        @Override
        public GerasLocationMonitor[] newArray(int size) {
            return new GerasLocationMonitor[size];
        }
    };
}
