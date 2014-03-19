package com.readystatesoftware.android.geras.mqtt;

/**
 * Created by jgilfelt on 19/03/2014.
 */
public class Geras {

    private String host = "geras.1248.io";
    private String apiKey;

    public Geras(String apiKey) {
        this.apiKey = apiKey;
    }

    public Geras(String host, String apiKey) {
        this(apiKey);
        this.host = host;
    }

    public void startService() {

    }

    public void stopService() {

    }

    public void publishDatapoint(String series, String message) {

    }



}
