package com.readystatesoftware.android.geras.mqtt;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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

import java.util.Locale;

public class GerasMQTTService extends Service implements MqttCallback {

    public static final String EXTRA_HOST = "host";
    public static final String EXTRA_API_KEY = "api_key";

    private static final String TAG = "GerasMQTTService";
    private static final int NOTIFICATION_ID = 1138;

    private static boolean sIsRunning = false;

    private NotificationManager mNotificationManager;
    private Handler mConnectionHandler;
    private HandlerThread mConnectionThread;
    private MqttClient mClient;
    private MqttDefaultFilePersistence mDataStore;
    private String mDeviceId;

    public static boolean isRunning() {
        return sIsRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceId = String.format("an_%s",
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mConnectionThread= new HandlerThread("mqtt-connection");
        mConnectionThread.start();
        mConnectionHandler = new Handler(mConnectionThread.getLooper());
        mDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = intent.getStringExtra(EXTRA_HOST);
        String apiKey = intent.getStringExtra(EXTRA_API_KEY);
        sIsRunning = true;
        showNotification();
        connect(host, apiKey);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        disconnect();
        removeNotification();
        mConnectionThread.quit();
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
                    mClient.subscribe("/time", 0);
                    Log.i(TAG,"Successfully connected");
                } catch(MqttException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void disconnect() {
        if(mClient != null) {
            mConnectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mClient.disconnect();
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
