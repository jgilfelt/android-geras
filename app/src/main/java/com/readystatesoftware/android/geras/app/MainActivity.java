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

package com.readystatesoftware.android.geras.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.readystatesoftware.android.geras.mqtt.GerasMqtt;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends Activity {

    private GerasMqtt geras;
    private SharedPreferences prefs;
    private SensorManager mgr;
    private ArrayAdapter<SensorListData> adapter;
    private ArrayList<SensorListData> data;

    EditText prefix;
    Spinner freq;
    TextView title;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geras = new GerasMqtt(getString(R.string.geras_api_key));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefix = (EditText) findViewById(R.id.prefix);
        freq = (Spinner) findViewById(R.id.freq);
        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(
                this, R.array.freqs, android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        freq.setAdapter(freqAdapter);
        mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        title = (TextView) findViewById(R.id.title);
        list = (ListView) findViewById(R.id.list);
        list.setItemsCanFocus(false);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        data = new ArrayList<SensorListData>();
        for (Map.Entry<Integer, String> entry : SensorMapConfig.SENSOR_MAP.entrySet()) {
            Sensor s = mgr.getDefaultSensor(entry.getKey());
            if (s != null) {
                data.add(new SensorListData(s.getName(), s.getType(), entry.getValue()));
            } else if (entry.getKey() == SensorMapConfig.TYPE_LOCATION_NETWORK) {
                data.add(new SensorListData("Network Location", entry.getKey(), entry.getValue()));
            } else if (entry.getKey() == SensorMapConfig.TYPE_LOCATION_GPS) {
                data.add(new SensorListData("GPS Location", entry.getKey(), entry.getValue()));
            }
        }
        adapter = new SensorListAdapter(this, data);
        list.setAdapter(adapter);

        if (geras.isServiceRunning()) {
            setUIEnabled(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreState();
    }

    private void prepare() {
        geras.clearSensorMonitors();
        geras.clearLocationMonitor();
        String seriesPrefix = prefix.getText().toString();
        SparseBooleanArray checked = list.getCheckedItemPositions();
        int sensorRate = 10 * 1000 * 1000;
        int locationTime = 60 * 1000;
        int locationDistance  = 100;
        switch (freq.getSelectedItemPosition()) {
            case 0:
                sensorRate = SensorManager.SENSOR_DELAY_FASTEST;
                locationTime = 0;
                locationDistance = 0;
                break;
            case 1:
                sensorRate = SensorManager.SENSOR_DELAY_NORMAL;
                locationTime = 10 * 1000;
                locationDistance = 10;
                break;
            case 2:
                break;
        }

        int i = 0;
        for (SensorListData d : data) {
            if (checked.get(i)) {
                if (d.getType() == SensorMapConfig.TYPE_LOCATION_NETWORK) {
                    geras.setLocationMonitor(seriesPrefix + d.getSeries(), LocationManager.NETWORK_PROVIDER, locationTime, locationDistance);
                } else if (d.getType() == SensorMapConfig.TYPE_LOCATION_GPS) {
                    geras.setLocationMonitor(seriesPrefix + d.getSeries(), LocationManager.GPS_PROVIDER, locationTime, locationDistance);
                } else {
                    geras.addSensorMonitor(seriesPrefix + d.getSeries(), d.getType(), sensorRate);
                }
            }
            i++;
        }
    }

    private void setUIEnabled(boolean enabled) {
        prefix.setEnabled(enabled);
        freq.setEnabled(enabled);
        list.setEnabled(enabled);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            title.setAlpha(enabled ? 1.0f : 0.5f);
            list.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    private void saveState() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("prefix", prefix.getText().toString());
        edit.putInt("freq", freq.getSelectedItemPosition());

        String checkedTypes = "";
        SparseBooleanArray checked = list.getCheckedItemPositions();
        int i = 0;
        for (SensorListData d : data) {
            if (checked.get(i)) {
                checkedTypes += "~" + i;
            }
            i++;
        }
        edit.putString("checked", checkedTypes);
        edit.apply();
    }

    private void restoreState() {
        prefix.setText(prefs.getString("prefix", null));
        freq.setSelection(prefs.getInt("freq", 1));
        String checkedTypes = prefs.getString("checked", "");
        String[] checked = checkedTypes.split("~");
        for (int i = 0; i < checked.length; i++) {
            try {
                int pos = Integer.valueOf(checked[i]);
                list.setItemChecked(pos, true);
            } catch (NumberFormatException e) {}
        }
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
        getMenuInflater().inflate(R.menu.main, menu);
        if (geras.isServiceRunning()) {
            menu.findItem(R.id.action_startstop).setIcon(R.drawable.ic_action_stop);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_startstop) {
            if (!geras.isServiceRunning()) {
                prepare();
                geras.startService(MainActivity.this);
                setUIEnabled(false);
                item.setIcon(R.drawable.ic_action_stop);
            } else {
                geras.stopService(MainActivity.this);
                setUIEnabled(true);
                item.setIcon(R.drawable.ic_action_start);
            }
            return true;
        } else if (id == R.id.action_publish) {
            if (geras.isServiceRunning()) {
                String seriesPrefix = prefix.getText().toString();
                geras.publishDatapoint(MainActivity.this, seriesPrefix + "/battery", String.valueOf(getBatteryLevel()));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SensorListAdapter extends ArrayAdapter<SensorListData> {

        SensorListAdapter(Context context, ArrayList<SensorListData> data) {
            super(context, android.R.layout.simple_list_item_multiple_choice, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = super.getView(position, convertView, parent);
            ((TextView)result).setText(getItem(position).getName());
            return (result);
        }

    }

}
