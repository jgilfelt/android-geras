android-geras
=============

Android realtime sensor feed for the 1248 [Geras][1] IoT time series database.

![screenshot](https://raw.github.com/jgilfelt/android-geras/master/app/screen.jpg "screenshot")

**NOTE: This project is experimental **

Project Structure
-----------------

#### geras-mqtt

Library module for realtime Geras publishing via MQTT using the Eclipse Paho client.

#### geras-rest

Client helpers for SenML and the Geras REST API using Gson, Retrofit and OkHttp.

#### app

Sample Android app that feeds selected [Sensor][2] data to Geras via MQTT at a given series path. Supply your Geras API key [here][3]. A long running service will establish a wake lock and maintain SensorEventListeners and an MQTT broker connection for its duration.

Example
-------

```java

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