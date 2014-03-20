package com.readystatesoftware.android.geras.mqtt;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
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

public class GerasMQTTService extends Service implements MqttCallback {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_API_KEY = "api_key";
    public static final String EXTRA_SENSOR_MONITORS = "sensor_monitors";
    public static final String EXTRA_LOCATION_MONTITOR = "location_monitor";

    private static final String TAG = "GerasMQTTService";
    private static final int NOTIFICATION_ID = 1138;

    private static boolean sIsRunning = false;

    private HashMap<Integer, GerasSensorMonitor> mSensorMap = new HashMap<Integer, GerasSensorMonitor>();

    private NotificationManager mNotificationManager;
    private SensorManager mSensorManager;
    private Handler mConnectionHandler;
    private HandlerThread mConnectionThread;
    private MqttClient mClient;
    private MqttDefaultFilePersistence mDataStore;
    private String mDeviceId;

    private SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                publishSensorValue(event.sensor.getType(), event.values);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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
        mConnectionThread = new HandlerThread("mqtt-connection");
        mConnectionThread.start();
        mConnectionHandler = new Handler(mConnectionThread.getLooper());
        mDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = intent.getStringExtra(EXTRA_HOST);
        String apiKey = intent.getStringExtra(EXTRA_API_KEY);
        ArrayList<GerasSensorMonitor> monitors = intent.getParcelableArrayListExtra(EXTRA_SENSOR_MONITORS);
        for(GerasSensorMonitor m : monitors) {
            mSensorMap.put(m.getSensorType(), m);
        }

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
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setContentTitle("Geras service running");
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
                    mClient.setCallback(GerasMQTTService.this);
                    //mClient.subscribe("/time", 0);

                    for(GerasSensorMonitor m : mSensorMap.values()) {
                        mSensorManager.registerListener(
                                mSensorListener,
                                mSensorManager.getDefaultSensor(m.getSensorType()),
                                m.getRateUs());
                    }

                    Log.i(TAG,"Successfully connected");
                } catch(MqttException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void publishSensorValue(final int sensorType, final float[] values) {
        if (mClient != null && mClient.isConnected()) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    GerasSensorMonitor m = mSensorMap.get(sensorType);
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

    private void disconnect() {
        mSensorManager.unregisterListener(mSensorListener);
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

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG,"  Topic:\t" + topic +
                "  Message:\t" + new String(message.getPayload()) +
                "  QoS:\t" + message.getQos());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

}
