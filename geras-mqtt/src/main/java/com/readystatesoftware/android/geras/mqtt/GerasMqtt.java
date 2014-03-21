package com.readystatesoftware.android.geras.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Created by jgilfelt on 19/03/2014.
 */
public class GerasMqtt {

    private String host = "geras.1248.io";
    private String apiKey;

    private ArrayList<GerasSensorConfig> mSensorMonitors = new ArrayList<GerasSensorConfig>();
    private GerasLocationConfig mLocationMonitor;

    public GerasMqtt(String apiKey) {
        this.apiKey = apiKey;
    }

    public GerasMqtt(String host, String apiKey) {
        this(apiKey);
        this.host = host;
    }

    public void startService(Context context) {
        if (!isServiceRunning()) {
            Intent intent = new Intent(context, GerasMqttService.class);
            intent.putExtra(GerasMqttService.EXTRA_HOST, host);
            intent.putExtra(GerasMqttService.EXTRA_API_KEY, apiKey);
            intent.putExtra(GerasMqttService.EXTRA_SENSOR_MONITORS, mSensorMonitors);
            intent.putExtra(GerasMqttService.EXTRA_LOCATION_MONTITOR, mLocationMonitor);
            context.startService(intent);
        }
    }

    public void stopService(Context context) {
        Intent intent = new Intent(context, GerasMqttService.class);
        context.stopService(intent);
    }

    public boolean isServiceRunning() {
        return GerasMqttService.isRunning();
    }

    public void publishDatapoint(Context context, String series, String value) {
        if (isServiceRunning()) {
            Intent intent = new Intent(GerasMqttService.ACTION_PUBLISH_DATAPOINT);
            intent.putExtra(GerasMqttService.EXTRA_DATAPOINT_SERIES, series);
            intent.putExtra(GerasMqttService.EXTRA_DATAPOINT_VALUE, value);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else {
            throw new IllegalStateException("You must call startService before publishing.");
        }
    }

    public void addSensorMonitor(String series, int sensorType, int rateUs) {
        if (!isServiceRunning()) {
            GerasSensorConfig m = new GerasSensorConfig(series, sensorType, rateUs);
            mSensorMonitors.add(m);
        } else {
            throw new IllegalStateException("You can only call addSensorMonitor prior to starting the service.");
        }
    }

    public void setLocationMonitor(String series, String provider, long minTime, float minDistance) {
        if (!isServiceRunning()) {
            mLocationMonitor = new GerasLocationConfig(series, provider, minTime, minDistance);
        } else {
            throw new IllegalStateException("You can only call setLocationMonitor prior to starting the service.");
        }
    }

    public void clearSensorMonitors() {
        if (!isServiceRunning()) {
            mSensorMonitors = new ArrayList<GerasSensorConfig>();
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
