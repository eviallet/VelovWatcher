package com.gueg.velovwidget;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;

import static com.gueg.velovwidget.WidgetItem.DATABASE_NAME;


@Entity(tableName = DATABASE_NAME, primaryKeys = {"number", "name"})
public class WidgetItem {
    public static final String DATABASE_NAME = "widget_items";
    public static final String CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contract_name";
    public static final String NO_CONTRACT_NAME = "com.gueg.velovwatcher.sharedprefs.contract_name.none";

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    @NonNull
    public Integer number;
    public String contract_name;
    @NonNull
    public String name;
    public String address;
    @TypeConverters(Converter.class)
    public Position position;

    public boolean isPinned;
    public int rank;


    @Ignore
    public DynamicData data;

    public WidgetItem(@NonNull Integer number, String contract_name, @NonNull String name, String address, Position position, boolean isPinned, int rank) {
        this.number = number;
        this.contract_name = contract_name;
        this.name = name;
        this.address = address;
        this.position = position;
        this.isPinned = isPinned;
        this.rank = rank;
    }

    public boolean isOpen() {
        return data!=null&&data.status.equals(STATUS_OPEN);
    }



    @Override
    public String toString() {
        return "number = "+number+" - name = "+name+" - address = "+address+" - latitude = "+position.lat+" - longitude = "+position.lng+" - isPinned = "+isPinned+" - rank = "+rank;
    }

    public static BoundingBox getBoundaries(ArrayList<WidgetItem> items) {
        double latMin = items.get(0).position.lat;
        double latMax = items.get(0).position.lat;
        double lngMin = items.get(0).position.lng;
        double lngMax = items.get(0).position.lng;

        for(WidgetItem item : items) {
            if(item.position.lat<latMin)
                latMin = item.position.lat;
            else if(item.position.lat>latMax)
                latMax = item.position.lat;
            if(item.position.lng<lngMin)
                lngMin = item.position.lng;
            else if(item.position.lng>lngMax)
                lngMax = item.position.lng;
        }
        return new BoundingBox(latMax, lngMax, latMin, lngMin);
    }

    public static WidgetItem findByName(ArrayList<WidgetItem> items, String name) {
        for(WidgetItem item : items)
            if(item.name.equals(name))
                return item;
        return null;
    }

    public static String getSelectedContract(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONTRACT_NAME, NO_CONTRACT_NAME);
    }

    static class Position {
        public double lat;
        public double lng;
    }
}
