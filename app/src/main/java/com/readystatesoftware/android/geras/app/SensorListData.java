package com.readystatesoftware.android.geras.app;

/**
 * Created by jgilfelt on 21/03/2014.
 */
public class SensorListData {
    private String name;
    private int type;
    private String series;

    public SensorListData(String name, int type, String series) {
        this.name = name;
        this.type = type;
        this.series = series;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getSeries() {
        return series;
    }

}
