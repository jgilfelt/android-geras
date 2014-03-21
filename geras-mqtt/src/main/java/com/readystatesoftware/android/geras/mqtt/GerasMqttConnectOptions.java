package com.readystatesoftware.android.geras.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Created by jgilfelt on 19/03/2014.
 *
 * Hack to allow the Paho MQTT Java client to accept an empty username required for GerasMqtt auth.
 */
public class GerasMqttConnectOptions extends MqttConnectOptions {

    private String userName;

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
