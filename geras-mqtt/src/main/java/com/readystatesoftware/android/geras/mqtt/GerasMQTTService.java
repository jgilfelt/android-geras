package com.readystatesoftware.android.geras.mqtt;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class GerasMqttService extends Service implements MqttCallback, SensorEventListener, LocationListener {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_API_KEY = "api_key";
    public static final String EXTRA_SENSOR_MONITORS = "sensor_monitors";
    public static final String EXTRA_LOCATION_MONTITOR = "location_monitor";

    public static final String ACTION_PUBLISH_DATAPOINT = "publish_datapoint";
    public static final String EXTRA_DATAPOINT_SERIES = "datapoint_series";
    public static final String EXTRA_DATAPOINT_VALUE = "datapoint_value";

    private static final String TAG = "GerasMqttService";
    private static final int NOTIFICATION_ID = 1138;

    private static boolean sIsRunning = false;

    private HashMap<Integer, GerasSensorConfig> mSensorMonitorMap = new HashMap<Integer, GerasSensorConfig>();
    private GerasLocationConfig mLocationMonitor;

    private NotificationManager mNotificationManager;
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Handler mConnectionHandler;
    private HandlerThread mConnectionThread;
    private MqttClient mClient;
    private MqttDefaultFilePersistence mDataStore;
    private String mDeviceId;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String series = intent.getStringExtra(EXTRA_DATAPOINT_SERIES);
            final String value = intent.getStringExtra(EXTRA_DATAPOINT_VALUE);
            publishDatapoint(series, value);
        }
    };

    public static boolean isRunning() {
        return sIsRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceId = String.format("an_%s",
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mConnectionThread = new HandlerThread("mqtt-connection");
        mConnectionThread.start();
        mConnectionHandler = new Handler(mConnectionThread.getLooper());
        mDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());

        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_PUBLISH_DATAPOINT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, f);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = intent.getStringExtra(EXTRA_HOST);
        String apiKey = intent.getStringExtra(EXTRA_API_KEY);
        ArrayList<GerasSensorConfig> monitors = intent.getParcelableArrayListExtra(EXTRA_SENSOR_MONITORS);
        for(GerasSensorConfig m : monitors) {
            mSensorMonitorMap.put(m.getSensorType(), m);
        }
        mLocationMonitor = intent.getParcelableExtra(EXTRA_LOCATION_MONTITOR);
        sIsRunning = true;
        showNotification();
        connect(host, apiKey);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        disconnect();
        removeNotification();
        //mConnectionThread.quit();
        sIsRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void showNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle("Geras MQTT service running")
                .setContentText("Sensors are active")
                .setSmallIcon(R.drawable.ic_stat_active);
        // issue the notification
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        // cancel the notification
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void connect(String host, String apiKey) {

        String url = String.format(Locale.US, "tcp://%s:%d", host, 1883);
        Log.i(TAG, "Connecting MQTT with URL: " + url);

        final GerasMqttConnectOptions opts = new GerasMqttConnectOptions();
        opts.setCleanSession(true);
        opts.setUserName("");
        opts.setPassword(apiKey.toCharArray());

        try {
            mClient = new MqttClient(url, mDeviceId, mDataStore);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mConnectionHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mClient.connect(opts);
                    mClient.setCallback(GerasMqttService.this);
                    //mClient.subscribe("/time", 0);

                    for(GerasSensorConfig m : mSensorMonitorMap.values()) {
                        mSensorManager.registerListener(
                                GerasMqttService.this,
                                mSensorManager.getDefaultSensor(m.getSensorType()),
                                m.getRateUs());
                    }

                    if (mLocationMonitor != null) {
                        mLocationManager.requestLocationUpdates(
                                mLocationMonitor.getProvider(),
                                mLocationMonitor.getMinTime(),
                                mLocationMonitor.getMinDistance(),
                                GerasMqttService.this);
                        Location last = mLocationManager.getLastKnownLocation(mLocationMonitor.getProvider());
                        if (last != null) {
                            publishLocationValue(last);
                        }
                    }

                    Log.i(TAG,"Successfully connected");
                } catch(MqttException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void publishDatapoint(final String series, final String value) {
        if (mClient != null && mClient.isConnected()) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "pub " + series + ":" + value);
                        mClient.publish(series, value.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void publishSensorValue(final int sensorType, final float[] values) {
        if (mClient != null && mClient.isConnected()) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    GerasSensorConfig m = mSensorMonitorMap.get(sensorType);
                    if (m != null) {
                        final String series = m.getSeries();
                        String value;
                        if (values.length >= 3) {
                            // calculate rms
                            value = String.valueOf(Math.sqrt(values[0] * values[0] +
                                    values[1] * values[1] +
                                    values[2] * values[2]));
                        } else {
                            value = String.valueOf(values[0]);
                        }
                        try {
                            Log.i(TAG, "pub " + series + ":" + value);
                            mClient.publish(series, value.getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void publishLocationValue(final Location location) {
        if (mClient != null && mClient.isConnected()) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    final String seriesLat = mLocationMonitor.getSeries() + "/latitude";
                    final String seriesLng = mLocationMonitor.getSeries() + "/longitude";
                    final String valueLat = String.valueOf(location.getLatitude());
                    final String valueLng = String.valueOf(location.getLongitude());
                    try {
                        Log.i(TAG, "pub " + seriesLat + ":" + valueLat);
                        Log.i(TAG, "pub " + seriesLng + ":" + valueLng);
                        mClient.publish(seriesLat, valueLat.getBytes(), 0, false);
                        mClient.publish(seriesLng, valueLng.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void disconnect() {
        mSensorManager.unregisterListener(this);
        mLocationManager.removeUpdates(this);
        if (mClient != null && mClient.isConnected()) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mClient.disconnect(0);
                        mClient = null;
                        Log.i(TAG,"Successfully disconnected");
                    } catch(MqttException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    // *** MQTT client callbacks ***

    @Override
    public void connectionLost(Throwable cause) {
        // TODO implement
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG,"  Topic:\t" + topic +
                "  Message:\t" + new String(message.getPayload()) +
                "  QoS:\t" + message.getQos());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // ignored
    }

    // *** sensor callbacks ***

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            publishSensorValue(event.sensor.getType(), event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignored
    }

    // *** location callbacks ***

    @Override
    public void onLocationChanged(Location location) {
        publishLocationValue(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ignored
    }

    @Override
    public void onProviderEnabled(String provider) {
        // ignored
    }

    @Override
    public void onProviderDisabled(String provider) {
        // ignored
    }
}
