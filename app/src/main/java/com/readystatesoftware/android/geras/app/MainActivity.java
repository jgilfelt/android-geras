package com.readystatesoftware.android.geras.app;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.readystatesoftware.android.geras.mqtt.Geras;
import com.readystatesoftware.android.geras.mqtt.GerasMQTTService;


public class MainActivity extends Activity {

    Button startStop;
    Button publish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Geras geras = new Geras(getString(R.string.geras_api_key));

        //geras.addSensorMonitor("/android/accelerometer", Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL);
        geras.addSensorMonitor("/android/light", Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL);
        //geras.addSensorMonitor("/android/pressure", Sensor.TYPE_PRESSURE, SensorManager.SENSOR_DELAY_NORMAL);

        geras.setLocationMonitor("/android/location", LocationManager.GPS_PROVIDER, 0, 0);

        startStop = (Button) findViewById(R.id.startstop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!geras.isServiceRunning()) {
                    geras.startService(MainActivity.this);
                } else {
                    geras.stopService(MainActivity.this);
                }
            }
        });

        publish = (Button) findViewById(R.id.publish);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (geras.isServiceRunning()) {
                    geras.publishDatapoint(MainActivity.this, "/android/battery", String.valueOf(getBatteryLevel()));
                }
            }
        });

    }

    private float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float)level / (float)scale) * 100.0f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
