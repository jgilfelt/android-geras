package com.readystatesoftware.android.geras.app;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.readystatesoftware.android.geras.mqtt.Geras;
import com.readystatesoftware.android.geras.mqtt.GerasMQTTService;


public class MainActivity extends Activity {

    Button startStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Geras geras = new Geras(getString(R.string.geras_api_key));

        geras.monitorSensor("/android/accelerometer", Sensor.TYPE_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL);
        geras.monitorSensor("/android/light", Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL);
        geras.monitorSensor("/android/pressure", Sensor.TYPE_PRESSURE, SensorManager.SENSOR_DELAY_NORMAL);

        startStop = (Button) findViewById(R.id.startstop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GerasMQTTService.isRunning()) {
                    geras.startService(MainActivity.this);
                } else {
                    geras.stopService(MainActivity.this);
                }
            }
        });

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
