package com.gueg.velovwidget.database_stations;


public class DynamicData {
    public String status;
    public int bike_stands;
    public int available_bike_stands;
    public int available_bikes;
    public String last_update;
    public boolean connected;

    public DynamicData(String status, int bike_stands, int available_bike_stands, int available_bikes, String last_update, boolean connected) {
        this.status = status;
        this.bike_stands = bike_stands;
        this.available_bike_stands = available_bike_stands;
        this.available_bikes = available_bikes;
        this.last_update = last_update;
        this.connected = connected;
    }
}
