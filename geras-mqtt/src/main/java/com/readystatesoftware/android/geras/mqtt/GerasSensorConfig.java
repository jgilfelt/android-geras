/*
 * Copyright (C) 2014 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.android.geras.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

public class GerasSensorConfig implements Parcelable {

    private String mSeries;
    private int mSensorType;
    private int mRateUs;

    public GerasSensorConfig(String series, int sensorType, int rateUs) {
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

    protected GerasSensorConfig(Parcel in) {
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
    public static final Parcelable.Creator<GerasSensorConfig> CREATOR = new Parcelable.Creator<GerasSensorConfig>() {
        @Override
        public GerasSensorConfig createFromParcel(Parcel in) {
            return new GerasSensorConfig(in);
        }

        @Override
        public GerasSensorConfig[] newArray(int size) {
            return new GerasSensorConfig[size];
        }
    };
}
