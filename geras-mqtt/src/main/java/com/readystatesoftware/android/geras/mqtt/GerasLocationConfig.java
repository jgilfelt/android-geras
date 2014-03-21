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

public class GerasLocationConfig implements Parcelable {

    private String mSeries;
    private String mProvider;
    private long mMinTime;
    private float mMinDistance;

    public GerasLocationConfig(String series, String provider, long minTime, float minDistance) {
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

    protected GerasLocationConfig(Parcel in) {
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
    public static final Parcelable.Creator<GerasLocationConfig> CREATOR = new Parcelable.Creator<GerasLocationConfig>() {
        @Override
        public GerasLocationConfig createFromParcel(Parcel in) {
            return new GerasLocationConfig(in);
        }

        @Override
        public GerasLocationConfig[] newArray(int size) {
            return new GerasLocationConfig[size];
        }
    };
}
