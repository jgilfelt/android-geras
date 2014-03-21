package com.readystatesoftware.android.geras.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Created by jgilfelt on 19/03/2014.
 */
public class Geras {

    private String host = "geras.1248.io";
    private String apiKey;

    private ArrayList<GerasSensorMonitor> mSensorMonitors = new ArrayList<GerasSensorMonitor>();
    private GerasLocationMonitor mLocationMonitor;

    public Geras(String apiKey) {
        this.apiKey = apiKey;
    }

    public Geras(String host, String apiKey) {
        this(apiKey);
        this.host = host;
    }

    public void startService(Context context) {
        if (!isServiceRunning()) {
            Intent intent = new Intent(context, GerasMQTTService.class);
            intent.putExtra(GerasMQTTService.EXTRA_HOST, host);
            intent.putExtra(GerasMQTTService.EXTRA_API_KEY, apiKey);
            intent.putExtra(GerasMQTTService.EXTRA_SENSOR_MONITORS, mSensorMonitors);
            intent.putExtra(GerasMQTTService.EXTRA_LOCATION_MONTITOR, mLocationMonitor);
            context.startService(intent);
        }
    }

    public void stopService(Context context) {
        Intent intent = new Intent(context, GerasMQTTService.class);
        context.stopService(intent);
    }

    public boolean isServiceRunning() {
        return GerasMQTTService.isRunning();
    }

    public void publishDatapoint(Context context, String series, String value) {
        if (isServiceRunning()) {
            Intent intent = new Intent(GerasMQTTService.ACTION_PUBLISH_DATAPOINT);
            intent.putExtra(GerasMQTTService.EXTRA_DATAPOINT_SERIES, series);
            intent.putExtra(GerasMQTTService.EXTRA_DATAPOINT_VALUE, value);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else {
            throw new IllegalStateException("You must call startService() before publishing.");
        }
    }

    public void addSensorMonitor(String series, int sensorType, int rateUs) {
        if (!isServiceRunning()) {
            GerasSensorMonitor m = new GerasSensorMonitor(series, sensorType, rateUs);
            mSensorMonitors.add(m);
        } else {
            throw new IllegalStateException("You can only call addSensorMonitor prior to starting the service.");
        }
    }

    public void setLocationMonitor(String series, String provider, long minTime, float minDistance) {
        if (!isServiceRunning()) {
            mLocationMonitor = new GerasLocationMonitor(series, provider, minTime, minDistance);
        } else {
            throw new IllegalStateException("You can only call setLocationMonitor prior to starting the service.");
        }
    }

    public void clearSensorMonitors() {
        if (!isServiceRunning()) {
            mSensorMonitors = new ArrayList<GerasSensorMonitor>();
        } else {
            throw new IllegalStateException("You can only call clearSensorMonitors prior to starting the service.");
        }
    }

    public void clearLocationMonitor() {
        if (!isServiceRunning()) {
            mLocationMonitor = null;
        } else {
            throw new IllegalStateException("You can only call clearLocationMonitor prior to starting the service.");
        }
    }

}
