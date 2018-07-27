package com.gueg.velovwidget;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;

@Entity(tableName = DATABASE_NAME)
public class WidgetItem {
    public static final String DATABASE_NAME = "widget_items";

    @PrimaryKey @NonNull
    public String name;
    /*
    public String address;
    public float lat;
    public float lng;
    public boolean banking;


    @Ignore
    public String status;
    @Ignore
    public int bike_stands;
    @Ignore
    public int available_bike_stands;
    @Ignore
    public int available_bikes;
    @Ignore
    public long last_update;

*/

    public WidgetItem(@NonNull String name) {
        this.name = name;
    }
/*
    public WidgetItem(@NonNull String name, String address, float lat, float lng, boolean banking, boolean bonus) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.banking = banking;
        this.bonus = bonus;
    }


    public void updateDynamicData(String status, int bike_stands, int available_bike_stands, int available_bikes, long last_update) {
        this.status = status;
        this.bike_stands = bike_stands;
        this.available_bike_stands = available_bike_stands;
        this.available_bikes = available_bikes;
        this.last_update = last_update;
    }
    */
}
