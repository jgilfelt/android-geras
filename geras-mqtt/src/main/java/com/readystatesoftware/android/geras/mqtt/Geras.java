package com.readystatesoftware.android.geras.mqtt;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jgilfelt on 19/03/2014.
 */
public class Geras {

    private String host = "geras.1248.io";
    private String apiKey;

    private ArrayList<GerasSensorMonitor> mSensorMonitors = new ArrayList<GerasSensorMonitor>();

    public Geras(String apiKey) {
        this.apiKey = apiKey;
    }

    public Geras(String host, String apiKey) {
        this(apiKey);
        this.host = host;
    }

    public void startService(Context context) {
        Intent intent = new Intent(context, GerasMQTTService.class);
        intent.putExtra(GerasMQTTService.EXTRA_HOST, host);
        intent.putExtra(GerasMQTTService.EXTRA_API_KEY, apiKey);
        intent.putExtra(GerasMQTTService.EXTRA_SENSOR_MONITORS, mSensorMonitors);
        context.startService(intent);
    }

    public void stopService(Context context) {
        Intent intent = new Intent(context, GerasMQTTService.class);
        context.stopService(intent);
    }

    public void publishDatapoint(String series, String message) {

    }

    public void monitorSensor(String series, int sensorType, int rateUs) {
        GerasSensorMonitor m = new GerasSensorMonitor(series, sensorType, rateUs);
        mSensorMonitors.add(m);
    }

    public void monitorLocation(String seriesLat, String seriesLng, String provider, long frequency) {

    }


}
