package com.readystatesoftware.android.geras.mqtt;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jgilfelt on 19/03/2014.
 */
public class Geras {

    private String host = "geras.1248.io";
    private String apiKey;

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
        context.startService(intent);
    }

    public void stopService(Context context) {
        Intent intent = new Intent(context, GerasMQTTService.class);
        context.stopService(intent);
    }

    public void publishDatapoint(String series, String message) {

    }

    public void monitorSensor(String series, int sensor, long frequency) {

    }

    public void monitorLocation(String seriesLat, String seriesLng, String provider, long frequency) {

    }


}
