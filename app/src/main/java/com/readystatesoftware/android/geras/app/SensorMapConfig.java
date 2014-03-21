package com.readystatesoftware.android.geras.app;

import android.hardware.Sensor;
import android.os.Build;

import java.util.HashMap;

/**
 * Created by jgilfelt on 21/03/2014.
 */
public class SensorMapConfig {

    public static final int TYPE_LOCATION_NETWORK = 7001;
    public static final int TYPE_LOCATION_GPS = 7002;

    public static HashMap<Integer, String> SENSOR_MAP = new HashMap<Integer, String>();

    static {
        SENSOR_MAP.put(Sensor.TYPE_ACCELEROMETER, "/accelerometer");
        SENSOR_MAP.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "/ambient-temperature");
        SENSOR_MAP.put(Sensor.TYPE_GRAVITY, "/gravity");
        SENSOR_MAP.put(Sensor.TYPE_GYROSCOPE, "/gyroscope");
        SENSOR_MAP.put(Sensor.TYPE_LIGHT, "/light");
        SENSOR_MAP.put(Sensor.TYPE_LINEAR_ACCELERATION, "/linear-acceleration");
        SENSOR_MAP.put(Sensor.TYPE_MAGNETIC_FIELD, "/magnetic-field");
        SENSOR_MAP.put(Sensor.TYPE_PRESSURE, "/pressure");
        SENSOR_MAP.put(Sensor.TYPE_PROXIMITY, "/proximity");
        SENSOR_MAP.put(Sensor.TYPE_RELATIVE_HUMIDITY, "/relative-humidity");
        SENSOR_MAP.put(Sensor.TYPE_ROTATION_VECTOR, "/rotation-vector");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            SENSOR_MAP.put(Sensor.TYPE_GAME_ROTATION_VECTOR, "/game-rotation-vector");
            SENSOR_MAP.put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "/gyroscope-uncalibrated");
            SENSOR_MAP.put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "/magnetic-field-uncalibrated");
            SENSOR_MAP.put(Sensor.TYPE_SIGNIFICANT_MOTION, "/significant-motion");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SENSOR_MAP.put(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "/geomagnetic-rotation-vector");
            SENSOR_MAP.put(Sensor.TYPE_STEP_COUNTER, "/step-counter");
            SENSOR_MAP.put(Sensor.TYPE_STEP_DETECTOR, "/step-detector");
        }
        SENSOR_MAP.put(TYPE_LOCATION_NETWORK, "/location-network");
        SENSOR_MAP.put(TYPE_LOCATION_GPS, "/location-gps");
    }

}
