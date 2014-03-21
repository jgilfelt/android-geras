android-geras
=============

Android realtime sensor feed for the 1248 [Geras][1] IoT time series database.

![screenshot](https://raw.github.com/jgilfelt/android-geras/master/app/screen.jpg "screenshot")

**NOTE: This project is experimental**


Project Structure
-----------------

#### geras-mqtt

Library module for realtime Geras publishing via MQTT using the Eclipse Paho client.

#### geras-rest

Client helpers for SenML and the Geras REST API using Gson, Retrofit and OkHttp.

#### app

Android app that feeds selected [Sensor][2] data to Geras via MQTT at a given series path. Supply your Geras API key as a resource [value][3]. A long running service will establish a wake lock and maintain SensorEventListeners and an MQTT broker connection for its duration.

Example
-------

```java
// Create a Geras MQTT instance using your API key
GerasMqtt geras = new GerasMqtt("YOUR_GERAS_API_KEY");

// Add sensor monitors
// Note: sensors that return values in three dimensions will publish the root mean square
geras.addSensorMonitor("/foo/temperature", Sensor.TYPE_AMBIENT_TEMPERATURE, SensorManager.SENSOR_DELAY_NORMAL);
geras.addSensorMonitor("/foo/humidity", Sensor.TYPE_RELATIVE_HUMIDITY, SensorManager.SENSOR_DELAY_NORMAL);
geras.addSensorMonitor("/foo/light", Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL);

// Set a location monitor
// Note: lat/lng coordinates will be published as discrete values
geras.setLocationMonitor("/foo/location", LocationManager.GPS_PROVIDER, 60, 10);

// Start the service to connect to the broker and start the sensor monitors
geras.startService(this);

// Publish an ad-hoc datapoint
// Useful for non-sensor information like microphone PCM data or battery level
geras.publishDatapoint(this, "/foo/battery", String.valueOf(getBatteryLevel()));

// Stop the service
geras.stopService(this);
```

License
-------

    Copyright (C) 2014 readyState Software Ltd

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: http://geras.1248.io
 [2]: https://developer.android.com/reference/android/hardware/Sensor.html
 [3]: https://github.com/jgilfelt/android-geras/blob/master/app/src/main/res/values/apikey.xml