package com.readystatesoftware.android.geras.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jgilfelt on 20/03/2014.
 */
public class GerasSensorMonitor implements Parcelable {

    private String mSeries;
    private int mSensorType;
    private int mRateUs;

    public GerasSensorMonitor(String series, int sensorType, int rateUs) {
        this.mSeries = series;
        this.mSensorType = sensorType;
        this.mRateUs = rateUs;
    }

    public String getSeries() {
        return mSeries;
    }

    public int getSensorType() {
        return mSensorType;
    }

    public int getRateUs() {
        return mRateUs;
    }

    protected GerasSensorMonitor(Parcel in) {
        mSeries = in.readString();
        mSensorType = in.readInt();
        mRateUs = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSeries);
        dest.writeInt(mSensorType);
        dest.writeInt(mRateUs);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GerasSensorMonitor> CREATOR = new Parcelable.Creator<GerasSensorMonitor>() {
        @Override
        public GerasSensorMonitor createFromParcel(Parcel in) {
            return new GerasSensorMonitor(in);
        }

        @Override
        public GerasSensorMonitor[] newArray(int size) {
            return new GerasSensorMonitor[size];
        }
    };
}
